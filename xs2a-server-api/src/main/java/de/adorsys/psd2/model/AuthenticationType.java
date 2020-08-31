package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Type of the authentication method.  More authentication types might be added during implementation projects and documented in the ASPSP documentation.    - &#39;SMS_OTP&#39;: An SCA method, where an OTP linked to the transaction to be authorised is sent to the PSU through a SMS channel.   - &#39;CHIP_OTP&#39;: An SCA method, where an OTP is generated by a chip card, e.g. a TOP derived from an EMV cryptogram.      To contact the card, the PSU normally needs a (handheld) device.      With this device, the PSU either reads the challenging data through a visual interface like flickering or      the PSU types in the challenge through the device key pad.      The device then derives an OTP from the challenge data and displays the OTP to the PSU.   - &#39;PHOTO_OTP&#39;: An SCA method, where the challenge is a QR code or similar encoded visual data      which can be read in by a consumer device or specific mobile app.      The device resp. the specific app than derives an OTP from the visual challenge data and displays      the OTP to the PSU.   - &#39;PUSH_OTP&#39;: An OTP is pushed to a dedicated authentication APP and displayed to the PSU.
 */
@ApiModel(description = "Type of the authentication method.  More authentication types might be added during implementation projects and documented in the ASPSP documentation.    - 'SMS_OTP': An SCA method, where an OTP linked to the transaction to be authorised is sent to the PSU through a SMS channel.   - 'CHIP_OTP': An SCA method, where an OTP is generated by a chip card, e.g. a TOP derived from an EMV cryptogram.      To contact the card, the PSU normally needs a (handheld) device.      With this device, the PSU either reads the challenging data through a visual interface like flickering or      the PSU types in the challenge through the device key pad.      The device then derives an OTP from the challenge data and displays the OTP to the PSU.   - 'PHOTO_OTP': An SCA method, where the challenge is a QR code or similar encoded visual data      which can be read in by a consumer device or specific mobile app.      The device resp. the specific app than derives an OTP from the visual challenge data and displays      the OTP to the PSU.   - 'PUSH_OTP': An OTP is pushed to a dedicated authentication APP and displayed to the PSU. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-08-31T16:39:54.348465+03:00[Europe/Kiev]")

public class AuthenticationType   {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AuthenticationType {\n");

    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

