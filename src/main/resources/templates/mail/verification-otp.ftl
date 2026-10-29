<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="color-scheme" content="light">
    <meta name="supported-color-schemes" content="light">
    <title>Verify Your Footprint Account</title>
</head>

<body style="margin:0; padding:0; background-color:#f3f6fa; font-family:Arial,Helvetica,sans-serif; color:#172033;">

<table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"
       style="background-color:#f3f6fa; padding:36px 12px;">
    <tr>
        <td align="center">

            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"
                   style="max-width:560px; background-color:#ffffff; border-radius:14px; overflow:hidden;">

                <!-- Header -->
                <tr>
                    <td align="center" style="padding:30px 24px; background-color:#102a43;">
                        <div style="font-size:25px; font-weight:700; letter-spacing:1px; color:#ffffff;">
                            Footprint
                        </div>

                        <p style="margin:8px 0 0; font-size:13px; color:#cbd5e1; line-height:20px;">
                            Secure account verification
                        </p>
                    </td>
                </tr>

                <!-- Main content -->
                <tr>
                    <td style="padding:36px 30px 30px;">

                        <h1 style="margin:0 0 18px; font-size:25px; line-height:34px; color:#102a43;">
                            Verify your account
                        </h1>

                        <p style="margin:0 0 16px; font-size:15px; line-height:25px; color:#526174;">
                            Hello <strong style="color:#172033;">${name?html}</strong>,
                        </p>

                        <p style="margin:0 0 26px; font-size:15px; line-height:25px; color:#526174;">
                            We received a request to verify your Footprint account.
                            Enter the verification code below to complete the process.
                        </p>

                        <!-- OTP panel -->
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                                <td align="center"
                                    style="padding:26px 16px; background-color:#f0f7ff; border:1px solid #d9e8f8; border-radius:12px;">

                                    <p style="margin:0 0 14px; font-size:11px; font-weight:700; letter-spacing:2px; color:#526174;">
                                        YOUR VERIFICATION CODE
                                    </p>

                                    <div style="font-size:34px; line-height:44px; font-weight:700; letter-spacing:7px; color:#1769aa;">
                                        ${otpCode?html}
                                    </div>

                                    <p style="margin:14px 0 0; font-size:13px; line-height:20px; color:#64748b;">
                                        This code expires in
                                        <strong>${otpExpiryMinutes?c} minutes</strong>.
                                    </p>

                                </td>
                            </tr>
                        </table>

                        <!-- Security notice -->
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0"
                               style="margin-top:24px;">
                            <tr>
                                <td style="padding:15px 16px; background-color:#fff8e8; border-left:3px solid #f0b429;">
                                    <p style="margin:0; font-size:13px; line-height:22px; color:#785b16;">
                                        <strong>Keep your code private.</strong>
                                        Footprint will never ask you to share your verification code.
                                        If you did not initiate this request, please ignore this email.
                                    </p>
                                </td>
                            </tr>
                        </table>

                        <p style="margin:26px 0 0; font-size:14px; line-height:23px; color:#526174;">
                            Thank you,<br>
                            <strong style="color:#172033;">The Footprint Team</strong>
                        </p>

                    </td>
                </tr>

                <!-- Footer -->
                <tr>
                    <td align="center"
                        style="padding:22px 24px; background-color:#f8fafc; border-top:1px solid #edf1f5;">

                        <p style="margin:0 0 8px; font-size:12px; line-height:19px; color:#64748b;">
                            This is an automated security email. Please do not reply.
                        </p>

                        <p style="margin:0; font-size:11px; line-height:18px; color:#94a3b8;">
                            &copy; Footprint. All rights reserved.
                        </p>

                    </td>
                </tr>

            </table>

        </td>
    </tr>
</table>

</body>
</html>