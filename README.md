# AndroidScopedStorageSample

**KO** Android 10(API 29)부터 적용된 Scoped Storage에서 **MediaStore**와 **Storage Access Framework(SAF)**로 파일을 다루는 샘플 프로젝트입니다.
**EN** A sample project showing how to work with files under Scoped Storage (Android 10 / API 29+) using **MediaStore** and the **Storage Access Framework (SAF)**.

> 📖 **Blog / 블로그**
> - [MediaStore File 다루기 (Android ScopedStorage)](https://dnight.tistory.com/entry/MediaStore-File-%EB%8B%A4%EB%A3%A8%EA%B8%B0-Android-ScopedStorage) (2019.12) — **KO** 이 샘플의 MediaStore 부분 · **EN** the MediaStore part of this sample
> - [Storage Access Framwork 다루기 (Android ScopedStorage)](https://dnight.tistory.com/entry/Storage-Access-Framwork-%EB%8B%A4%EB%A3%A8%EA%B8%B0-Android-ScopedStorage) (2019.12) — **KO** SAF 부분 · **EN** the SAF part

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
| 폴더 선택·파일 생성 (SAF) / Pick a folder & create files (SAF) | `Intent.ACTION_OPEN_DOCUMENT_TREE`, `Intent.ACTION_CREATE_DOCUMENT` + `DocumentFile` |

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

## Status / 현재 상태

**KO** 빌드 설정: minSdk 16, compileSdk/targetSdk 29 (Android 10 기준으로 작성).
**EN** Build config: minSdk 16, compileSdk/targetSdk 29 (written for Android 10).

### Known issues in 2019 / 작성 당시 알려진 문제

- **KO** 2019년 글에는 삭제 후에도 파일이 실제로 지워지지 않는 문제와 조회 시 `DATE_TAKEN`이 1970-01-01로 나오는 문제가 적혀 있습니다. 이슈 트래커: [142270549](https://issuetracker.google.com/issues/142270549), [145348304](https://issuetracker.google.com/issues/145348304).
  **EN** The 2019 post notes that deleted files weren't actually removed and that `DATE_TAKEN` returned 1970-01-01. Issue tracker: [142270549](https://issuetracker.google.com/issues/142270549), [145348304](https://issuetracker.google.com/issues/145348304).

### Changes after Android 10 / Android 10 이후 바뀐 점

**KO** 이 샘플에는 반영되어 있지 않습니다. 최신 SDK를 타깃으로 할 때 참고하세요.
**EN** These are not reflected in the sample. Keep them in mind when targeting a newer SDK.

- **KO** Android 11(API 30): targetSdk 30 이상이면 `requestLegacyExternalStorage`가 무시됩니다. 여러 파일의 삭제·수정은 `MediaStore.createDeleteRequest()` / `createWriteRequest()` / `createTrashRequest()`로 사용자 동의를 한 번에 받을 수 있습니다.
  **EN** Android 11 (API 30): with targetSdk 30+, `requestLegacyExternalStorage` is ignored. Use `MediaStore.createDeleteRequest()` / `createWriteRequest()` / `createTrashRequest()` to get user consent for several files at once.
- **KO** Android 11: `ACTION_OPEN_DOCUMENT_TREE`로 저장소 루트, `Download` 폴더, `Android/data`, `Android/obb`는 선택할 수 없습니다.
  **EN** Android 11: `ACTION_OPEN_DOCUMENT_TREE` can no longer select the storage root, the `Download` folder, `Android/data` or `Android/obb`.
- **KO** Android 13(API 33): 미디어 읽기 권한이 `READ_EXTERNAL_STORAGE` 대신 `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO`로 나뉩니다.
  **EN** Android 13 (API 33): media read access moves from `READ_EXTERNAL_STORAGE` to `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` / `READ_MEDIA_AUDIO`.

---

## References / 참고 링크

- [Scoped storage (Android Developers)](https://developer.android.com/training/data-storage/files/external-scoped)
- [Storage updates in Android 11 (Android Developers)](https://developer.android.com/about/versions/11/privacy/storage)
- [안드로이드 Q Scoped Storage 이해하기](https://brunch.co.kr/@huewu/8)
- [안드로이드 Q Scoped Storage에서 살아남기](https://brunch.co.kr/@huewu/14)

---

## 📚 Related posts / 관련 글

- [Android 고유식별자 (unique identifiers)](https://dnight.tistory.com/entry/Android-%EA%B3%A0%EC%9C%A0%EC%8B%9D%EB%B3%84%EC%9E%90) — **KO** Android 10에서 달라진 식별자 · **EN** Identifier changes in Android 10
- [Android 카테고리 전체 / All Android posts](https://dnight.tistory.com/category/Android)
