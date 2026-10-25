# AndroidScopedStorageSample

**KO** Android 10(API 29)부터 적용된 Scoped Storage에서 **MediaStore**와 **Storage Access Framework(SAF)**로 파일을 다루는 샘플 프로젝트입니다.
**EN** A sample project showing how to work with files under Scoped Storage (Android 10 / API 29+) using **MediaStore** and the **Storage Access Framework (SAF)**.

📖 Blog / 블로그
- [MediaStore File 다루기 (Android ScopedStorage)](https://dnight.tistory.com/entry/MediaStore-File-%EB%8B%A4%EB%A3%A8%EA%B8%B0-Android-ScopedStorage) (2019.12)
- [Storage Access Framwork 다루기 (Android ScopedStorage)](https://dnight.tistory.com/entry/Storage-Access-Framwork-%EB%8B%A4%EB%A3%A8%EA%B8%B0-Android-ScopedStorage) (2019.12)

> ⚠️ **KO** 샘플과 글은 2019년(Android 10) 기준입니다. 그 이후 API 변경은 아래 "Status" 항목을 확인하세요.
> ⚠️ **EN** The sample and posts date from 2019 (Android 10). See "Status" below for later API changes.

<!-- TODO: 스크린샷 추가 / add screenshots
![screenshot](docs/screenshot.png)
-->

---

## What it shows / 데모 기능

| 기능 / Feature | 방법 / How |
|---|---|
| 파일 탐색·읽기 / List & read | `ContentResolver.query()` + `MediaStore.{Images,Audio,Video}.Media.EXTERNAL_CONTENT_URI` |
| 파일 생성 / Create | `ContentValues` (`RELATIVE_PATH`, `DISPLAY_NAME`, `MIME_TYPE`, `IS_PENDING`) → `insert()` |
| 파일 삭제 / Delete | `ContentResolver.delete()`, 다른 앱 파일은 `RecoverableSecurityException` 처리 / handle `RecoverableSecurityException` for other apps' files |
| 다운로드 폴더 등 / Downloads etc. | Storage Access Framework (SAF) |

---

## Access rules (Android 10) / 접근 규칙

- **KO** 앱별 디렉터리: 앱을 지우면 파일도 함께 삭제됩니다.
  **EN** App-specific directory: files are removed when the app is uninstalled.
- **KO** 미디어 컬렉션: 지정된 폴더에만 만들 수 있습니다.
  **EN** Media collections: files can only be created in designated folders.

| Type / 종류 | Allowed directories / 허용 폴더 |
|---|---|
| Image | `DCIM`, `Pictures` |
| Audio | `Alarms`, `Music`, `Notifications`, `Podcasts`, `Ringtones` |
| Video | `DCIM`, `Movies` |

**KO** 허용되지 않은 경로에 만들면 `IllegalArgumentException: Primary directory (invalid) not allowed ...`가 발생합니다.
**EN** Creating a file elsewhere throws `IllegalArgumentException: Primary directory (invalid) not allowed ...`.

- **KO** 다운로드 폴더는 SAF로 접근합니다. 그 외 기존의 자유로운 외부 저장소 접근은 막힙니다.
  **EN** Use SAF for the Downloads folder. Other free-form external-storage access is blocked.
- **KO** 임시로 `android:requestLegacyExternalStorage="true"`로 예전 방식을 유지할 수 있지만, 이후 Scoped Storage로 전환해야 합니다.
  **EN** `android:requestLegacyExternalStorage="true"` temporarily keeps the old behavior, but you'll need to migrate eventually.

---

## Code / 코드

### Create / 파일 생성 (요약 / excerpt)

```kotlin
val values = ContentValues().apply {
    put(MediaStore.Files.FileColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/image")
    put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
    put(MediaStore.Files.FileColumns.MIME_TYPE, "image/*")
    put(MediaStore.Files.FileColumns.IS_PENDING, 1)
}
val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
// write the bytes through openFileDescriptor(uri, "w"), then:
values.clear()
values.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
context.contentResolver.update(uri!!, values, null, null)
```

### Delete / 파일 삭제 (요약 / excerpt)

```kotlin
try {
    context.contentResolver.delete(uri, null, null)
} catch (e: RecoverableSecurityException) {
    // ask the user for permission, then retry from onActivityResult
    val sender = e.userAction.actionIntent.intentSender
    ActivityCompat.startIntentSenderForResult(
        context as Activity, sender, DELETE_PERMISSION_REQUEST, null, 0, 0, 0, null
    )
}
```

**KO** 전체 코드는 `app` 모듈을 참고하세요.
**EN** See the `app` module for the full code.

---

## Status / 현재 상태 (확인 필요 / to verify)

<!-- TODO: 아래 항목을 최신 Android 기준으로 확인한 뒤 내용을 정리하세요 / verify against current Android and update -->

- **KO** 2019년 글에는 삭제 후에도 파일이 실제로 지워지지 않는 문제와 조회 시 `DATE_TAKEN`이 1970-01-01로 나오는 문제가 적혀 있습니다. 이슈 트래커: [142270549](https://issuetracker.google.com/issues/142270549), [145348304](https://issuetracker.google.com/issues/145348304).
  **EN** The 2019 post notes that deleted files weren't actually removed and that `DATE_TAKEN` returned 1970-01-01. Issue tracker: [142270549](https://issuetracker.google.com/issues/142270549), [145348304](https://issuetracker.google.com/issues/145348304).
- **KO** Android 11(API 30) 이후 삭제·쓰기 요청 방식(`MediaStore.createDeleteRequest` 등)과 `requestLegacyExternalStorage` 동작이 바뀌었는지 확인하세요.
  **EN** Check whether Android 11+ changes (e.g. `MediaStore.createDeleteRequest`) and the behavior of `requestLegacyExternalStorage` apply to this sample.
- **KO** 테스트한 기기/OS: ______ **EN** Tested on: ______

---

## References / 참고 링크

- [Scoped storage (Android Developers)](https://developer.android.com/training/data-storage/files/external-scoped)
- [안드로이드 Q Scoped Storage 이해하기](https://brunch.co.kr/@huewu/8)
- [안드로이드 Q Scoped Storage에서 살아남기](https://brunch.co.kr/@huewu/14)
