# Forgot password

[← Design index](../README.md) · Route `forgotPassword` · `ui/composable/forgotpassword/ForgotPasswordScreen.kt` · `ForgotPasswordViewModel`

A three-step flow on a single screen. Each step replaces the one before it. The top bar shows **Forgot Password** with a back button.

| 1. Request code | 2. Verify code | 3. Change password |
|---|---|---|
| <img src="../images/forgotpassword/RequestCodePreview_light.png" width="240"> | <img src="../images/forgotpassword/VerifyCodePreview_dark.png" width="240"> | <img src="../images/forgotpassword/ChangePasswordPreview_light.png" width="240"> |
| Enter email → **Request code**. If the address is registered, a code is emailed. | Enter the code from the email → **Verify code** | Enter and confirm the new password → **Change password** |

```mermaid
flowchart LR
    A[Request code] -->|code sent| B[Verify code]
    B -->|code matches| C[Change password]
    C -->|PasswordReset — cleared| L[Login]
```

Each field shows inline validation messages. A `loading` flag disables input while a request is in flight. `UnexpectedError` shows a snackbar.
