# RotatePassword

一个仿密码箱的双转动密码转盘。

![](http://881023.top/image/RotatePassword.gif)

## 说明

研究自定义view的产出项目。

## 使用

    <com.kiana.sjt.library.RotatePassword
        android:id="@+id/rp"
        android:layout_width="300dp"
        android:layout_height="300dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
        
```
rotatePassword = findViewById(R.id.rp);
        rotatePassword.setCorrectNumber(5,10, true);
        rotatePassword.setOnCircleChangedListener(new RotatePassword.OnCircleChangedListener() {
            @Override
            public void onChanged(int outerNumber, int innerNumber) {

            }

            @Override
            public void isCorrect(int outerNumber, int innerNumber) {
                Toast.makeText(MainActivity.this, "密码正确："+outerNumber+":"+innerNumber, Toast.LENGTH_LONG).show();
                Log.d("RotatePassword", "密码正确："+outerNumber+":"+innerNumber);
            }
        });
```
