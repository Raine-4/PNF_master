# PNF Master 

## Language 

- [简体中文](#简体中文)
- [English](#english)
- [繁體中文](#繁體中文)

---

### 简体中文

---

## 1  应用介绍

北航（现在已经没了的）国通学院机械专业的个人毕业设计。团队毕设是制作一个基于PNF疗法的下肢康复机器人，我个人负责的部分是开发一个Android应用，基本功能是能通过蓝牙控制机器人（上的电机）实现患者自主康复训练。拓展功能计划包括云端存储用户数据，同一账户可以在不同设备上登录，以获得相同的个人资料。而个人资料可以作为程序为患者定制个性化康复计划的依据，这个功能之后可能会接入一个AI的API来实现。

## 2 开发日志

### 2.1 需要联网使用的功能

1. LoginActivity：点击登录按钮之后需要等待一秒钟；
2. NewUserActivity：点击下一步时需要检测用户名是否已存在，需要等待一秒；
3. RehabInfoActivity：上传至数据库；
4. ProfileActivity：打开的时候需要调取数据库的信息；

### 2.2 更新日志

小标题为更新日期。

#### - 24/4/01 更新

- 目前存在的问题：

1. 当用户以未登录状态进入后，再点击退出登录。此时再登录会变成“未登录用户”。
2. 打开蓝牙时需要请求 android.permission.BLUETOOTH_SCAN permission。现在只能手动打开.
3. 每次打开软件都需要重新连接蓝牙

- 准备添加的功能

1. 制作用户界面：包含绘图和操作按钮
2. 重写帮助内容
3. 编写设置页面

- 可能添加的功能

1. 定时发送通知，提示用还未完成今天的任务（当然也可以在设置里关闭）
2. 可以通过点击头像来更换头像，头像需要存储至云端数据库
3. 云端数据和本地数据库并存.这样就可以仅第一次打开较为耗时，之后打开直接从本地数据库读取，速度会快非常多。

---
### English
---

## 1 Introduction

A graduation project in Mechanical Engineering at Beihang University. The project is part of a team project to create a lower limb rehabilitation robot based on the Proprioceptive Neuromuscular Facilitation (PNF) therapy. My specific responsibility is to build an Android app that allows patients to control the robot's motors via Bluetooth, enabling autonomous rehabilitation training. In addition to the basic functionality, the app may be implemented some advanced features such as cloud storage for user data. Users will be able to log in to the same account from different devices, ensuring consistent access to their personal profiles. These profiles will serve as the basis for customizing personalized rehabilitation plans for patients. Furthermore, the idea of integrating an AI component to enhance the system's capabilities in the future is being considered.

## 2 Developing Log

### 2.1 Funcations that Need Internet

1. LoginActivity: 1 sec is needed after clicking Login button；
2. NewUserActivity：check whether the username is already existed；
3. RehabInfoActivity：upload info onto the database；
4. ProfileActivity：fetch the database information；

### 2.2 Updating Log

Subtitle is the updating date.

#### - April 1st Updates

- Current Issues：

1. When a user enters as an unlogged-in user, click Log Out. When the user logs in again, it will become "Unlogged in user".
2. android.permission.BLUETOOTH_SCAN permission is needed when opening BluetoothScanningActivity for the first time. Only manully open the bluetooth is possible now.
3. Every time users open the app, they need to reconnect bluetooth.

- Features to be added

1. User Interface：including drawings and buttons for one-click operations.
2. Rewrite the help content.
3. Write the setting page.

- Possible Features

1. Send notifications to remind users that they haven't finished today's tasks yet (can be turned off in the settings of course)
2. Users can change the avatar by clicking on the avatar, the avatar needs to be stored in the cloud database.
3. Cloud and local databases are stored together. So that only the first time to open the more time-consuming, and then open directly from the local database to read, the speed will be much faster.


---
### 繁體中文
---

## 1 應用介紹

北航（現在已經沒了的）國通學院機械專業的個人畢業設計。團隊畢設是製作一個基於PNF療法的下肢康復機器人，我個人負責的部分是開發一個Android應用程式，基本功能是能通過藍牙控制機器人（上的電機）實現患者自主康復訓練。拓展功能計劃包括雲端存儲用戶數據，同一賬戶可以在不同設備上登錄，以獲得相同的個人資料。而個人資料可以作為程序為患者定制個性化康復計劃的依據，這個功能之後可能會接入一個AI的API來實現。

## 2 開發日誌

### 2.1 需要聯網使用的功能

1. LoginActivity：點擊登錄按鈕之後需要等待一秒鐘；
2. NewUserActivity：點擊下一步時需要檢測用戶名是否已存在，需要等待一秒；
3. RehabInfoActivity：上傳至數據庫；
4. ProfileActivity：打開的時候需要調取數據庫的訊息；

### 2.2 更新日誌

小標題為更新日期。

#### - 24/4/01 更新

- 目前存在的問題：

1. 當用戶以未登錄狀態進入後，再點擊退出登錄。此時再登錄會變成“未登錄用戶”。
2. 打開藍牙時需要請求 android.permission.BLUETOOTH_SCAN permission。現在只能手動打開。
3. 每次打開軟體都需要重新連接藍牙。

- 準備添加的功能：

1. 製作用戶界面：包含繪圖和操作按鈕。
2. 重寫幫助內容。
3. 編寫設置頁面。

- 可能添加的功能：

1. 定時發送通知，提示用戶還未完成今天的任務（當然也可以在設置裡關閉）。
2. 可以透過點擊大頭貼來更換，大頭貼需要存儲至雲端數據庫。
3. 雲端數據和本地數據庫並存。這樣就可以僅第一次打開較為耗時，之後打開直接從本地數據庫讀取，速度會快非常多。