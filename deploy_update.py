import os
import re
import sys
import json
import subprocess
try:
    from google.cloud import storage
except ImportError:
    print("La biblioteca 'google-cloud-storage' no está instalada.")
    print("Por favor ejecute: pip install google-cloud-storage")
    sys.exit(1)

# Configuration
BUCKET_NAME = "androidblejoy.firebasestorage.app"
SERVICE_ACCOUNT_FILE = "service-account.json"
GRADLE_FILE = os.path.join("app", "build.gradle.kts")
APK_PATH = os.path.join("app", "build", "outputs", "apk", "debug", "app-debug.apk")
UPDATE_JSON_FILE = "update.json"

def get_current_version_details():
    if not os.path.exists(GRADLE_FILE):
        print(f"Error: No se encontró {GRADLE_FILE}")
        sys.exit(1)
        
    with open(GRADLE_FILE, 'r', encoding='utf-8') as f:
        content = f.read()
        
    code_match = re.search(r'versionCode\s*=\s*(\d+)', content)
    name_match = re.search(r'versionName\s*=\s*"([^"]+)"', content)
    
    if not code_match or not name_match:
        print("Error: No se pudo parsear versionCode o versionName en build.gradle.kts")
        sys.exit(1)
        
    return int(code_match.group(1)), name_match.group(1), content

def update_gradle_version(content, next_code, next_name):
    content = re.sub(r'versionCode\s*=\s*\d+', f'versionCode = {next_code}', content)
    content = re.sub(r'versionName\s*=\s*"[^"]+"', f'versionName = "{next_name}"', content)
    
    with open(GRADLE_FILE, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f"-> Actualizado build.gradle.kts a versionCode={next_code}, versionName='{next_name}'")

def increment_version_name(version_name):
    parts = version_name.split('.')
    try:
        parts[-1] = str(int(parts[-1]) + 1)
        return '.'.join(parts)
    except ValueError:
        return version_name + ".1"

def compile_apk():
    print("-> Compilando la aplicación (gradlew assembleDebug)...")
    # Use gradlew.bat on Windows, ./gradlew on Unix
    gradle_cmd = "gradlew.bat" if os.name == 'nt' else "./gradlew"
    
    try:
        result = subprocess.run([gradle_cmd, "assembleDebug"], check=True)
        if result.returncode == 0:
            print("-> Compilación exitosa.")
            return True
    except Exception as e:
        print(f"Error al compilar la app: {e}")
        return False
    return False

def upload_to_firebase(next_code, next_name, release_notes):
    if not os.path.exists(SERVICE_ACCOUNT_FILE):
        print(f"Error: Archivo de credenciales '{SERVICE_ACCOUNT_FILE}' no encontrado.")
        sys.exit(1)

    print("-> Inicializando Firebase Storage...")
    client = storage.Client.from_service_account_json(SERVICE_ACCOUNT_FILE)
    bucket = client.bucket(BUCKET_NAME)

    # 1. Upload APK
    apk_blob_name = "updates/app-debug.apk"
    apk_blob = bucket.blob(apk_blob_name)
    
    print(f"-> Subiendo APK a updates/app-debug.apk...")
    apk_blob.upload_from_filename(APK_PATH, content_type="application/vnd.android.package-archive")
    apk_blob.make_public()
    
    # Generate public URLs
    # Use storage.googleapis.com public URL (bypasses Firebase Storage auth rules on public objects)
    apk_url = f"https://storage.googleapis.com/{BUCKET_NAME}/updates/app-debug.apk"

    
    print(f"-> APK subida. URL pública: {apk_url}")

    # 2. Generate and Upload update.json
    update_data = {
        "versionCode": next_code,
        "versionName": next_name,
        "apkUrl": apk_url,
        "releaseNotes": release_notes,
        "forceUpdate": False
    }

    with open(UPDATE_JSON_FILE, 'w', encoding='utf-8') as f:
        json.dump(update_data, f, indent=2, ensure_ascii=False)
        
    print(f"-> Generado {UPDATE_JSON_FILE} localmente.")
    
    json_blob_name = "updates/update.json"
    json_blob = bucket.blob(json_blob_name)
    # Prevent GCS from caching update metadata
    json_blob.cache_control = "no-cache, no-store, must-revalidate"
    
    print(f"-> Subiendo {UPDATE_JSON_FILE} a updates/update.json...")
    json_blob.upload_from_filename(UPDATE_JSON_FILE, content_type="application/json")
    json_blob.make_public()
    
    json_url = f"https://storage.googleapis.com/{BUCKET_NAME}/updates/update.json"
    print(f"-> JSON subido. URL pública: {json_url}")
    
    # Cleanup local update.json
    if os.path.exists(UPDATE_JSON_FILE):
        os.remove(UPDATE_JSON_FILE)
        
    print("\n¡Despliegue finalizado con éxito!")
    print(f"Nueva Versión: {next_name} ({next_code})")
    print(f"Notas: {release_notes}")

def main():
    current_code, current_name, gradle_content = get_current_version_details()
    print(f"Versión actual: {current_name} (versionCode: {current_code})")
    
    # Calculate next version defaults
    next_code = current_code + 1
    suggested_name = increment_version_name(current_name)
    
    # Prompt for input
    next_name_input = input(f"Ingrese el nombre de la nueva versión [{suggested_name}]: ").strip()
    next_name = next_name_input if next_name_input else suggested_name
    
    release_notes = input("Ingrese las notas de versión (cambios): ").strip()
    if not release_notes:
        release_notes = "Mejoras de rendimiento y corrección de errores."
        
    confirm = input(f"¿Confirmar incremento a {next_name} ({next_code}) y compilar/subir? (s/n): ").strip().lower()
    if confirm != 's':
        print("Cancelado por el usuario.")
        sys.exit(0)
        
    # Apply version increments to build.gradle.kts
    update_gradle_version(gradle_content, next_code, next_name)
    
    # Compile
    if not compile_apk():
        # Rollback version in build.gradle.kts if compilation failed
        print("-> Cancelando: Restaurando versión anterior...")
        update_gradle_version(gradle_content, current_code, current_name)
        sys.exit(1)
        
    # Upload
    try:
        upload_to_firebase(next_code, next_name, release_notes)
    except Exception as e:
        print(f"\nError durante la subida a Firebase: {e}")
        print("-> Restaurando versión anterior en build.gradle.kts...")
        update_gradle_version(gradle_content, current_code, current_name)
        sys.exit(1)

if __name__ == "__main__":
    main()
