import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

MethodChannel methodChannel = MethodChannel('flutter3d/demo1');

Future<int> getTextureId() {
  return methodChannel.invokeMethod<int>('getTextureId');
}

void main() => runApp(ExampleApp());

class ExampleApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Flutter 3D',
      theme: ThemeData(
        canvasColor: Colors.black,
      ),
      home: ExamplePage(),
    );
  }
}

class ExamplePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.black,
      child: Stack(
        children: <Widget>[
          FutureBuilder<int>(
            future: getTextureId(),
            builder: (BuildContext context, AsyncSnapshot<int> snapshot) {
              if (snapshot.hasData) {
                return Texture(
                  textureId: snapshot.data,
                );
              } else {
                return Center(
                  child: CircularProgressIndicator(),
                );
              }
            },
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: RaisedButton(
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (BuildContext context) {
                    return AlertDialog(
                      content: Text('It Works!'),
                      actions: <Widget>[
                        FlatButton(
                          onPressed: () => Navigator.of(context).pop(),
                          child: Text('CLOSE'),
                        ),
                      ],
                    );
                  },
                );
              },
              child: Text('Show'),
            ),
          ),
        ],
      ),
    );
  }
}
