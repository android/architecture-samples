import os
import google.auth
from google.auth.transport.requests import Request
from googleapiclient.discovery import build
# Load service account credentials
echo ${apkservice} >> nlp.json
credentials, project = google.auth.load_credentials_from_file('nlp.json')
# Initialize Google Play Developer API client
service = build('androidpublisher', 'v3', credentials=credentials)
# Upload APK
package_name = 'com.example.android.architecture.blueprints.todoapp'
track = 'alpha'  # 'alpha', 'beta', 'production', etc.
# Create an edit to upload the APK
edit_request = service.edits().insert(body={}, packageName=package_name)
edit = edit_request.execute()
# Upload APK file
apk_file_path = 'app/build/outputs/apk/release/*.apk'
apk_request = service.edits().apks().upload(
    editId=edit['id'],
    packageName=package_name,
    media_body=apk_file_path
)
apk_response = apk_request.execute()
# Commit the edit (finalize the APK upload)
commit_request = service.edits().commit(
    editId=edit['id'],
    packageName=package_name
)
commit_request.execute()
print("APK successfully uploaded to Google Play!")
