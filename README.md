# VoloX AI Android — Version 5.8

A minimal Android app for chatting with the existing VoloX AI backend.

## Build a free debug APK using GitHub Actions

1. Create a separate GitHub repository named `VoloX-AI-Android`.
2. Upload the contents of this ZIP to the repository root (the files inside this folder, not the folder itself).
3. Open **Actions** → **Build VoloX AI APK**.
4. Run the workflow with **Run workflow**, or push to `main`.
5. Open the completed workflow run and download the artifact named `VoloX-AI-5.8-debug-apk`.
6. Extract the artifact ZIP to get `app-debug.apk`, then install it on your Android phone.

## Backend connection

The app expects a reachable HTTPS backend with `POST /api/chat` accepting JSON:
`{"message":"Hello"}`

It expects a JSON response containing an `answer` string.

Your current Google Colab address is not automatically reachable from an Android app, and Colab sessions can stop. Deploy the backend to a secure HTTPS host before using the app remotely. Do not expose an unauthenticated development server to the public internet.

## Notes

- This workflow builds a **debug APK** for testing. A release-ready APK should be signed securely before distribution.
- This project is separate from `VoloX-Game`.
- The build workflow has been provided but has not been executed in your GitHub account from this ZIP.
