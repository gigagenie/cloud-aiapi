# KT Cloud AI API

KT Cloud AI API 서비스 소개 및 개발자 가이드를 제공합니다.

## 서비스 소개

KT Cloud AI API는 기가지니에서 제공하는 인공지능 엔진 기반 AI API입니다. 현재 지니 Dictation(음성인식),  지니 Voice(TTS) 및 지니 Custom Voice(개인화 TTS) API를 제공하며, 추후 더욱 다양한 AI API 제공을 확대해 나갈 예정입니다.

## 제공 서비스

KT AI API가 제공하는 서비스는 다음과 같습니다. 

먼저 AI API를 서비스하는 플랫폼의 형태에 따라 SaaS형과 PaaS형으로 나뉩니다. 플랫폼 형태에 따른 분류는 아래 표와 같습니다.

| 구분   | API형                  | PaaS형                                      |
| ------ | ---------------------- | ------------------------------------------- |
| 인프라 | KT 내부 인프라         | 전용 VM 및 인프라 구성                      |
| 요금   | API 사용량에 따라 과금 | 인프라 비용 및 API 라이선스비용에 따른 과금 |



KT AI API는 HTTP 및 gRPC 프로토콜을 지원합니다. 각각의 프로토콜에 대해 일반 API 및 SDK를 지원합니다. 서비스 제공 형태에 따른 분류는 아래 표와 같습니다. 

| 구분 | HTTP     | gRPC     |
| ---- | -------- | -------- |
| API  | HTTP API | gRPC API |
| SDK  | HTTP SDK | gRPC SDK |



## Supported Platform

KT Cloud AI API가 지원하는 개발 환경은 다음과 같습니다.

- API형
  - HTTP API : Rest API 형태의 API 제공	
  - gRPC API : Python, java, node.js, C#
- SDK형
  - HTTP SDK 및 gRPC API : Python, java, node.js, C#, Android, iOS
    - Android : android API Level >= 16 (target OS version : API Level 26)
    - iOS : iOS Deployment Target >= 7.1

## 배포 패키지 구성

- README.md : this file
- LICENSE.md : KT Cloud AI API 이용 약관
- NOTICE.md : 오픈소스 고지문
- RELEASE.md : 릴리즈 노트
- ai-api-sdk/ : AI API SDK 라이브러리 및 샘플 코드 (Python, java, node.js, C#, Android, iOS)

## 서비스 신청 및 관리

KT AI API 신청 및 관리 방법에 대해서는 KT Cloud 홈페이지의 [사용자 매뉴얼](https://cloud.kt.com/portal/user-guide/gigagenie_ai_api-gigagenie-aiapiregisterandmanagement)을 참고하세요.

## Developer Guide

API형 및 SDK형 상품의 개발자 가이드는 [wiki 페이지](https://github.com/gigagenie/cloud-aiapi/wiki)를 통해 제공됩니다.

