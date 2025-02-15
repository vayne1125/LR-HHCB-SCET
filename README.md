# LR-HHCB-SCET
適用於IIoT環境之具洩漏存活與相等性測試的異質基於憑證簽章加密機制</br>
Leakage-resilient heterogeneous hybrid certificate-based signcryption with equality test for IIoT environment
- 112-國科會大專學生計畫
- 編號: 112-2813-C-019 -015 -E

![image](https://github.com/vayne1125/LR-HHCB-SCET/assets/95048697/b44dfb90-0ac3-4c5e-ab63-5d0afa234b28)


## Environment

## Abstract

隨著邊緣運算、雲端運算、人工智慧等 IT 領域技術發展日漸成熟，藉助數位化的力量來推動企業轉型，已是各產業共通的課題。台灣製造業面對全球市場需求快速轉變與競爭壓力，數位化與商業模式轉型儼然成為維持競爭優勢的必要發展方向。工業物聯網（industrial internet of things, IIoT）作為物聯網（internet of things, IoT）的一個分支，迅速崛起。但仍有許多機構因為安全問題，未能成功部署工業物聯網及營運技術。基於現狀，我們不經對目前 IIoT 環境產生好奇，於是開始大量瀏覽 IIoT 在資安方面的論文，目的是想了解現在 IIoT 發展到什麼程度或是還存在哪些問題可以探討。其中有一篇論文讓我們很感興趣，是在 IEEE Internet of Things Journal 上看到的一篇論文，名稱為 __**適用於 IIoT 環境之具相等性測試的異質簽章加密機制（Heterogeneous signcryption with equality test for IIoT environment, HSC-ET for IIoT environment）**__ 。異質機制指的是存在兩種以上的密碼機制，像是在 HSC-ET 機制中，他的感測器是使用公開金鑰基礎建設（public-key infrastructure, PKI），而使用者端則是使用基於身分的密碼機制（identity-based cryptosystem, IBC），異質機制支持使用者在不同機制下可以對資料進行查詢和比對。但是我們認為現有的機制有幾個可以改良的地方:
1. 金鑰託管問題: 由於 HSC-ET 機制是使用基於身分的密碼機制，所以存在金鑰託管問題（key escrow problems）。我們認為可以改成使用基於憑證的密碼機制（certificate-based cryptosystem, CBC），解決這個問題。
2. 無法抵擋旁路攻擊: 攻擊者可以使用旁路攻擊（side-channel attacks）竊取部分私密金鑰的資訊。攻擊者持續收集這些部分的私密金鑰後是有可能還原完整的私鑰，導致機制失去安全性。因此，我們認為應該要建立一個可抵擋旁路攻擊的機制，亦即在部分的私密金鑰洩漏的狀況下，仍然可以確保此機制是安全的。

在本研究計畫中，我們提出一個全新的機制，名為 __**適用於 IIoT 環境之具洩漏存活與相等性測試的異質基於憑證簽章加密機制（Leakage-resilient heterogeneous hybrid ertificate-based signcryption with equality test for IIoT environment, LR-HHCB-SCET）**__ ，除了保留原始 HSC-ET 機制在異質環境下提供相等性測試的功能之外，還解決金鑰託管的問題且可抵擋旁路攻擊。除了提出新機制之外，本研究計畫也使用 Java 函式庫中的 Java Pairing Based Cryptography Library (JPBC)實作此機制並放在 Github 提供開源使用。

