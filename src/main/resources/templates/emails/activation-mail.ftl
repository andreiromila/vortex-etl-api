<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="UTF-8">
  <title>Vortex ETL Account Activation</title>
</head>

<body
  style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 0; background-color: #f3f4f6;">
  <!-- CORREGIDO: gray-100 -->

  <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f3f4f6;">
    <!-- CORREGIDO: gray-100 -->
    <tr>
      <td align="center">
        <table width="600" border="0" cellspacing="0" cellpadding="0"
          style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; border: 1px solid #e5e7eb;">

          <!-- === CABECERA === -->
          <tr>
            <td align="center"
              style="background-color: #4f46e5; padding: 24px 32px; border-top-left-radius: 11px; border-top-right-radius: 11px;">

              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <!-- Celda para el Logo -->
                  <td width="64" valign="middle">
                    <img width="48" height="48" alt="Vortex ETL Logo"
                      src="https://andreiromila.com/vortexetl/web-app-manifest-192x192.png"
                      style="display: block; border-radius: 8px;">
                  </td>
                  <!-- Celda para el Texto -->
                  <td valign="middle" style="padding-left: 8px;">
                    <h1 style="color: #ffffff; font-size: 24px; margin: 0; font-weight: 600; line-height: 1;">Vortex ETL</h1>
                    <!-- Color: indigo-200 -->
                  </td>
                </tr>
              </table>

            </td>
          </tr>

          <!-- CONTENIDO DEL EMAIL -->
          <tr>
            <td style="padding: 40px 50px;" align="center">
              <!-- TITULO -->
              <h1 style="font-size: 22px; color: #111827; margin-top: 0; margin-bottom: 36px; font-weight: bold;">Welcome, ${fullName}!</h1>
              <p style="font-size: 15px; color: #6b7280; line-height: 1.6;">An administrator has created an account for
                you. To complete your registration and secure your account, please set a password by clicking the button
                below.</p>
              <p style="font-size: 15px; color: #6b7280; line-height: 1.6;">This activation link is valid for 24 hours.
              </p>

              <!-- Botón CTA -->
              <table border="0" cellspacing="0" cellpadding="0" width="100%">
                <tr>
                  <td align="center" style="padding-top: 20px; padding-bottom: 20px;">
                    <a href="${activationLink}"
                      style="background-color: #4f46e5; color: #ffffff; text-decoration: none; padding: 15px 30px; border-radius: 8px; font-weight: bold; font-size: 14px; display: inline-block;">
                      Set Your Password
                    </a>
                  </td>
                </tr>
              </table>

              <p style="font-size: 12px; color: #6b7280; margin-top: 30px;">
                If you have trouble clicking the button, copy and paste this URL into your web browser:
              </p>
              <p style="font-size: 12px; word-break: break-all;">
                <a href="${activationLink}" style="color: #4f46e5; text-decoration: underline;">${activationLink}</a>
              </p>
            </td>
          </tr>

          <!-- FOOTER -->
          <tr>
            <td
              style="padding: 30px; text-align: center; font-size: 12px; color: #6b7280; background-color: #f9fafb; border-bottom-left-radius: 11px; border-bottom-right-radius: 11px;">
              <p style="margin: 0; padding: 0;">(ɔ) 2025 Vortex ETL by Andrei Romila. This is free software.</p>
              <p style="margin: 5px 0 0; padding: 0; font-size: 10px; color: #9ca3af;">
                You are free to use, modify, and distribute it. Provided as-is, without warranty.
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>

</body>

</html>