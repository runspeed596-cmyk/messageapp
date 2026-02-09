# 📬 راهنمای تست API با Postman

## ⚙️ تنظیمات اولیه

### 1. ایجاد Environment در Postman
Variable های زیر را اضافه کنید:
| Variable | Initial Value |
|----------|---------------|
| `base_url` | `http://localhost:8080` |
| `token` | (خالی - بعد از لاگین پر میشود) |
| `user_id` | (خالی) |

### 2. تنظیم Authorization
در تب "Authorization" هر Request:
- Type: `Bearer Token`
- Token: `{{token}}`

---

## 🔐 Authentication

### ارسال کد OTP
```
POST {{base_url}}/api/auth/send-otp
Content-Type: application/json

{
    "phoneNumber": "09123456789"
}
```
**Response:**
```json
{
    "success": true,
    "message": "کد تأیید ارسال شد",
    "expiresInSeconds": 300
}
```
> ⚠️ کد OTP در Console سرور چاپ میشود (برای تست)

---

### تأیید OTP و لاگین
```
POST {{base_url}}/api/auth/verify-otp
Content-Type: application/json

{
    "phoneNumber": "09123456789",
    "code": "123456"
}
```
**Response:**
```json
{
    "success": true,
    "message": "ورود موفق",
    "accessToken": "eyJhbGci...",
    "refreshToken": "abc-123-def",
    "user": { "id": "...", "displayName": "کاربر جدید" },
    "isNewUser": true
}
```
> 📝 مقدار `accessToken` را در Environment به عنوان `token` ذخیره کنید
> 📝 مقدار `user.id` را به عنوان `user_id` ذخیره کنید

---

### Refresh Token
```
POST {{base_url}}/api/auth/refresh-token
Content-Type: application/json

{
    "refreshToken": "abc-123-def"
}
```

---

### خروج
```
POST {{base_url}}/api/auth/logout
Authorization: Bearer {{token}}
```

---

## 👤 Users

### دریافت پروفایل من
```
GET {{base_url}}/api/users/me
Authorization: Bearer {{token}}
```

---

### بروزرسانی پروفایل
```
PUT {{base_url}}/api/users/me
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "username": "ali_hasani",
    "displayName": "علی حسنی",
    "bio": "برنامه نویس اندروید"
}
```

---

### آپلود آواتار
```
POST {{base_url}}/api/users/avatar
Authorization: Bearer {{token}}
Content-Type: multipart/form-data

file: [Select File]
```

---

### جستجوی کاربران
```
GET {{base_url}}/api/users/search?query=علی&page=0&size=20
Authorization: Bearer {{token}}
```

---

## 💬 Private Chats

### لیست چت‌ها
```
GET {{base_url}}/api/chats?page=0&size=50
Authorization: Bearer {{token}}
```

---

### ایجاد چت جدید
```
POST {{base_url}}/api/chats
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "participantId": "{{other_user_id}}"
}
```

---

### پین کردن چت
```
PUT {{base_url}}/api/chats/{{chat_id}}/pin?pinned=true
Authorization: Bearer {{token}}
```

---

### بی‌صدا کردن چت
```
PUT {{base_url}}/api/chats/{{chat_id}}/mute?muted=true
Authorization: Bearer {{token}}
```

---

## 📨 Messages

### دریافت پیام‌های چت
```
GET {{base_url}}/api/chats/{{chat_id}}/messages?page=0&size=50
Authorization: Bearer {{token}}
```

---

### ارسال پیام
```
POST {{base_url}}/api/chats/{{chat_id}}/messages
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "type": "TEXT",
    "content": "سلام! این یک پیام تست است 👋"
}
```

---

### ویرایش پیام
```
PUT {{base_url}}/api/messages/{{message_id}}
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "content": "پیام ویرایش شده"
}
```

---

### حذف پیام
```
DELETE {{base_url}}/api/messages/{{message_id}}
Authorization: Bearer {{token}}
```

---

## 👥 Groups

### لیست گروه‌ها
```
GET {{base_url}}/api/groups?page=0&size=50
Authorization: Bearer {{token}}
```

---

### ایجاد گروه
```
POST {{base_url}}/api/groups
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "name": "گروه تست",
    "description": "این یک گروه تست است",
    "isPublic": false,
    "memberIds": ["{{user_id_1}}", "{{user_id_2}}"]
}
```

---

### اضافه کردن اعضا
```
POST {{base_url}}/api/groups/{{group_id}}/members
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "memberIds": ["{{new_user_id}}"]
}
```

---

### ارسال پیام به گروه
```
POST {{base_url}}/api/groups/{{group_id}}/messages
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "type": "TEXT",
    "content": "سلام به همه!"
}
```

---

## 📢 Channels

### لیست کانال‌ها
```
GET {{base_url}}/api/channels?page=0&size=50
Authorization: Bearer {{token}}
```

---

### ایجاد کانال
```
POST {{base_url}}/api/channels
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "name": "کانال خبری",
    "description": "آخرین اخبار",
    "isPublic": true
}
```

---

### عضویت در کانال
```
POST {{base_url}}/api/channels/{{channel_id}}/subscribe
Authorization: Bearer {{token}}
```

---

### ارسال پست
```
POST {{base_url}}/api/channels/{{channel_id}}/posts
Authorization: Bearer {{token}}
Content-Type: application/json

{
    "content": "این اولین پست کانال است! 🎉",
    "commentsEnabled": true
}
```

---

### جستجوی کانال‌های عمومی
```
GET {{base_url}}/api/channels/search?query=خبر&page=0&size=20
Authorization: Bearer {{token}}
```

---

## 📁 File Upload

### آپلود فایل
```
POST {{base_url}}/api/files/upload
Authorization: Bearer {{token}}
Content-Type: multipart/form-data

file: [Select File]
```
**Response:**
```json
{
    "success": true,
    "message": "فایل آپلود شد",
    "data": "/uploads/abc-123_image.jpg"
}
```

---

## 📋 Swagger UI (جایگزین)

اگر ترجیح می‌دهید از Swagger استفاده کنید:
```
http://localhost:8080/swagger-ui.html
```

---

## 🔧 نکات مهم

1. **قبل از شروع:** دیتابیس PostgreSQL با نام `messageapp` بسازید
2. **کد OTP:** در Console سرور چاپ میشود: `OTP for 09123456789: 123456`
3. **Token:** توکن ۲۴ ساعت اعتبار دارد
4. **فایل‌ها:** حداکثر سایز ۱۰MB
