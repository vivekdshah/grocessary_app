import 'package:intl/intl.dart';

class DateConverter {
  static String formatDate(DateTime dateTime) {
    return DateFormat('yyyy-MM-dd hh:mm:ss').format(dateTime);
  }

  static String estimatedDate(DateTime dateTime) {
    return DateFormat('dd MMM yyyy').format(dateTime);
  }

  static DateTime convertStringToDatetime(String dateTime) {
    return DateFormat("yyyy-MM-ddTHH:mm:ss.SSS").parse(dateTime);
  }

  static DateTime isoStringToLocalDate(String dateTime) {
    return DateFormat('yyyy-MM-ddTHH:mm:ss.SSS').parse(dateTime, true).toLocal();
  }

  static DateTime dateToDDMMMyyyy(String dateTime) {
    return DateFormat('yyyy-MM-dd').parse(dateTime, true);
  }

  static String isoStringTodateToDDMMMyyyy(String dateTime) {
    return DateFormat('dd MMM yyyy').format(dateToDDMMMyyyy(dateTime));
  }

  static String isoStringToLocalTimeOnly(String dateTime) {
    return DateFormat('hh:mm aa').format(isoStringToLocalDate(dateTime));
  }
  static String isoStringToLocalAMPM(String dateTime) {
    return DateFormat('a').format(isoStringToLocalDate(dateTime));
  }

  static String isoStringToLocalDateOnly(String dateTime) {
    return DateFormat('dd MMM yyyy').format(isoStringToLocalDate(dateTime));
  }

  static String localDateToIsoString(DateTime dateTime) {
    return DateFormat('yyyy-MM-ddTHH:mm:ss.SSS').format(dateTime.toUtc());
  }

  static String convertTimeToTime(String time) {
    return DateFormat('hh:mm a').format(DateFormat('HH:mm:ss').parse(time));
  }

  static String formatPhoneNumber(String? phoneNumber) {
    phoneNumber = phoneNumber?.replaceAll('US', '');
    phoneNumber = phoneNumber?.replaceAll('+1', '');
    if (phoneNumber == null || phoneNumber.isEmpty) {
      return ''; // Return empty string for null or numbers with length less than 10
    }

    String countryCode = phoneNumber.substring(0, 3);
    String areaCode = phoneNumber.substring(3, 6);
    String remainingDigits = phoneNumber.substring(6);

    if (remainingDigits.length > 4) {
      String firstDigits = remainingDigits.substring(0, 3);
      String lastDigits = remainingDigits.substring(3);
      return '($countryCode) $areaCode-$firstDigits-$lastDigits';
    } else {
      return '($countryCode) $areaCode-$remainingDigits';
    }
  }
}
