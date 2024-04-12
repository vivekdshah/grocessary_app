import 'package:efood_kitchen/util/dimensions.dart';
import 'package:efood_kitchen/util/styles.dart';
import 'package:efood_kitchen/view/base/custom_button.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

class ConfirmationDialog extends StatelessWidget {
  final String icon;
  final String title;
  final String description;
  final String? yesButtonText;
  final Function onYesPressed;
  final Function? onNoPressed;
  const ConfirmationDialog({super.key, required this.icon, required this.title, required this.description, required this.onYesPressed,
    this.onNoPressed, this.yesButtonText});

  @override
  Widget build(BuildContext context) {
    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(Dimensions.radiusSmall)),
      insetPadding: const EdgeInsets.all(30),
      clipBehavior: Clip.antiAliasWithSaveLayer,
      child: SizedBox(width: 500, child: Padding(
        padding: const EdgeInsets.all(Dimensions.paddingSizeLarge),
        child: Column(mainAxisSize: MainAxisSize.min, children: [

          Padding(
            padding: const EdgeInsets.all(Dimensions.paddingSizeLarge),
            child: Image.asset(icon, width: 50, height: 50),
          ),

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: Dimensions.paddingSizeLarge),
            child: Text(
              title, textAlign: TextAlign.center,
              style: robotoMedium.copyWith(fontSize: Dimensions.fontSizeExtraLarge, color: Colors.red),
            ),
          ),

          Padding(
            padding: const EdgeInsets.all(Dimensions.paddingSizeLarge),
            child: Text(description, style: robotoMedium.copyWith(fontSize: Dimensions.fontSizeLarge), textAlign: TextAlign.center),
          ),
          const SizedBox(height: Dimensions.paddingSizeLarge),

          Row(children: [
            if(onNoPressed != null) Expanded(child: TextButton(
              onPressed: () => onNoPressed!(),
              style: TextButton.styleFrom(
                backgroundColor: Theme.of(context).disabledColor.withOpacity(0.3), minimumSize: const Size(Dimensions.webMaxWidth, 40), padding: EdgeInsets.zero,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(Dimensions.radiusSmall)),
              ),
              child: Text(
                'no'.tr, textAlign: TextAlign.center,
                style: robotoBold.copyWith(color: Theme.of(context).textTheme.bodyLarge?.color),
              ),
            )),
            const SizedBox(width: Dimensions.paddingSizeLarge),

            Expanded(child: CustomButton(
              buttonText: yesButtonText ?? 'yes'.tr,
              onPressed: () => onYesPressed(),
              radius: Dimensions.radiusSmall, height: 40,
            )),
          ]),

        ]),
      )),
    );
  }
}
