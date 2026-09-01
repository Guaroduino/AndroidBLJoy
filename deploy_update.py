import os
import re
import sys
import json
import shutil
import argparse
import subprocess

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
    gradle_cmd = "gradlew.bat" if os.name == 'nt' else "./gradlew"
    
    # Auto-detect Android Studio JBR if JAVA_HOME is not configured in Windows
    env = os.environ.copy()
    if "JAVA_HOME" not in env or not os.path.exists(env["JAVA_HOME"]):
        default_jbr = r"C:\Program Files\Android\Android Studio\jbr"
        if os.path.exists(default_jbr):
            env["JAVA_HOME"] = default_jbr
            env["PATH"] = os.path.join(default_jbr, "bin") + os.pathsep + env.get("PATH", "")
    
    try:
        result = subprocess.run([gradle_cmd, "assembleDebug"], check=True, env=env)
        if result.returncode == 0:
            print("-> Compilación exitosa.")
            return True
    except Exception as e:
        print(f"Error al compilar la app: {e}")
        return False
    return False

def generate_update_json(next_code, next_name, release_notes):
    apk_url = f"https://storage.googleapis.com/{BUCKET_NAME}/updates/app-debug.apk"
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
    return update_data

def upload_to_firebase(next_code, next_name, release_notes):
    if not os.path.exists(SERVICE_ACCOUNT_FILE):
        print("\n" + "=" * 70)
        print("  AVISO: 'service-account.json' no encontrado en la raíz.")
        print("=" * 70)
        print("Para que este script suba la actualización de forma 100% automática:")
        print("  1. Abre este enlace en tu navegador:")
        print(f"     https://console.firebase.google.com/project/androidblejoy/settings/serviceaccounts/adminsdk")
        print("  2. Haz clic en 'Generar nueva clave privada' y descarga el archivo JSON.")
        print(f"  3. Guárdalo como '{SERVICE_ACCOUNT_FILE}' en la carpeta raíz del proyecto.")
        print("\nPara publicar manualmente ahora:")
        print("  1. Abre la consola de Firebase Storage de 'androidblejoy':")
        print(f"     https://console.firebase.google.com/project/androidblejoy/storage")
        print("  2. En la carpeta 'updates/', sube:")
        print(f"     - '{APK_PATH}' (como 'app-debug.apk')")
        print(f"     - '{UPDATE_JSON_FILE}'")
        print("=" * 70)
        
        # Also copy APK to Desktop for convenience
        desktop_path = os.path.join(os.path.expanduser("~"), "Desktop", f"AndroidBLJoy_v{next_name}.apk")
        try:
            shutil.copyfile(APK_PATH, desktop_path)
            print(f"-> APK copiado al escritorio: {desktop_path}")
        except Exception:
            pass
        return False

    try:
        from google.cloud import storage
    except ImportError:
        print("La biblioteca 'google-cloud-storage' no está instalada.")
        print("Por favor ejecute: pip install google-cloud-storage")
        return False

    print("-> Inicializando Firebase Storage...")
    client = storage.Client.from_service_account_json(SERVICE_ACCOUNT_FILE)
    bucket = client.bucket(BUCKET_NAME)

    # 1. Upload APK
    apk_blob_name = "updates/app-debug.apk"
    apk_blob = bucket.blob(apk_blob_name)
    
    print(f"-> Subiendo APK a updates/app-debug.apk...")
    apk_blob.upload_from_filename(APK_PATH, content_type="application/vnd.android.package-archive")
    apk_blob.make_public()
    
    apk_url = f"https://storage.googleapis.com/{BUCKET_NAME}/updates/app-debug.apk"
    print(f"-> APK subida. URL pública: {apk_url}")

    # 2. Upload update.json
    json_blob_name = "updates/update.json"
    json_blob = bucket.blob(json_blob_name)
    json_blob.cache_control = "no-cache, no-store, must-revalidate"
    
    print(f"-> Subiendo {UPDATE_JSON_FILE} a updates/update.json...")
    json_blob.upload_from_filename(UPDATE_JSON_FILE, content_type="application/json")
    json_blob.make_public()
    
    json_url = f"https://storage.googleapis.com/{BUCKET_NAME}/updates/update.json"
    print(f"-> JSON subido. URL pública: {json_url}")
    
    print("\n¡Despliegue finalizado con éxito!")
    print(f"Nueva Versión: {next_name} ({next_code})")
    print(f"Notas: {release_notes}")
    return True

def main():
    parser = argparse.ArgumentParser(description="Publicador de actualizaciones OTA para AndroidBLJoy")
    parser.add_argument("--name", type=str, help="Nombre de la nueva versión (ej: 1.14)")
    parser.add_argument("--notes", type=str, help="Notas de la versión")
    parser.add_argument("--yes", "-y", action="store_true", help="Confirmar automáticamente sin preguntar")
    args = parser.parse_args()

    current_code, current_name, gradle_content = get_current_version_details()
    print(f"Versión actual: {current_name} (versionCode: {current_code})")
    
    next_code = current_code + 1
    suggested_name = increment_version_name(current_name)
    
    if args.name:
        next_name = args.name.strip()
    elif not sys.stdin.isatty():
        next_name = suggested_name
    else:
        next_name_input = input(f"Ingrese el nombre de la nueva versión [{suggested_name}]: ").strip()
        next_name = next_name_input if next_name_input else suggested_name
    
    default_notes = "Refactorización a Transmisor RC estándar con 5 pestañas, telemetría gráfica en tiempo real y Model Match."
    if args.notes:
        release_notes = args.notes.strip()
    elif not sys.stdin.isatty():
        release_notes = default_notes
    else:
        release_notes_input = input(f"Ingrese las notas de versión [{default_notes}]: ").strip()
        release_notes = release_notes_input if release_notes_input else default_notes
        
    if not args.yes and sys.stdin.isatty():
        confirm = input(f"¿Confirmar incremento a {next_name} ({next_code}) y compilar/subir? (s/n): ").strip().lower()
        if confirm != 's':
            print("Cancelado por el usuario.")
            sys.exit(0)
        
    # Apply version increments to build.gradle.kts
    update_gradle_version(gradle_content, next_code, next_name)
    
    # Compile
    if not compile_apk():
        print("-> Cancelando: Restaurando versión anterior...")
        update_gradle_version(gradle_content, current_code, current_name)
        sys.exit(1)
        
    # Generate local update.json
    generate_update_json(next_code, next_name, release_notes)

    # Upload
    try:
        upload_to_firebase(next_code, next_name, release_notes)
    except Exception as e:
        print(f"\nError durante la subida a Firebase: {e}")

if __name__ == "__main__":
    main()
