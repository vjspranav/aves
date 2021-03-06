import 'package:aves/widgets/common/icons.dart';
import 'package:flutter/material.dart';

class MenuRow extends StatelessWidget {
  final String text;
  final IconData icon;
  final bool checked;

  const MenuRow({
    Key key,
    this.text,
    this.icon,
    this.checked,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        if (checked != null) ...[
          Opacity(
            opacity: checked ? 1 : 0,
            child: Icon(AIcons.checked),
          ),
          SizedBox(width: 8),
        ],
        if (icon != null) ...[
          Icon(icon),
          SizedBox(width: 8),
        ],
        Expanded(child: Text(text)),
      ],
    );
  }
}
