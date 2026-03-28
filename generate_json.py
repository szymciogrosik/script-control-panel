import json, sys

def load_json(idx):
    try:
        return json.loads(sys.argv[idx]) if sys.argv[idx] and sys.argv[idx] != 'null' else None
    except json.JSONDecodeError:
        return None

stable_data = load_json(1)
pre_data = load_json(2)

def extract_info(release):
    if not release: return None
    if isinstance(release, list):
        if not release: return None
        release = release[0]
    
    version = release.get("tagName")
    if not version: return None
    published_at = release.get("publishedAt")
    
    download_url = f"https://github.com/szymciogrosik/script-control-panel/releases/download/{version}/ScriptControlPanel-Win.zip"
    
    return {
        "version": version,
        "downloadUrl": download_url,
        "releaseDate": published_at
    }

result = {
    "release": extract_info(stable_data),
    "preRelease": extract_info(pre_data)
}

with open("docs/latest_release.json", "w") as f:
    json.dump(result, f, indent=2)
