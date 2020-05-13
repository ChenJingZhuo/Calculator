package com.example.calculator;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calculator.util.Arith;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private final static String TAG = "MainActivity";
    private TextView mTvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        //初始化计算记录
        try {
            FileInputStream fis = new FileInputStream("record.txt");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            sb.append(baos.toString());
            fis.close();
        } catch (IOException e) {
        }
        mTvResult.setText(sb.toString()+"\n");
    }
    private boolean first=true;

    @Override
    public void onClick(View v) {
        int resid = v.getId(); // 获得当前按钮的编号
        String inputText;
        inputText = ((TextView) v).getText().toString();
        Log.d(TAG, "resid=" + resid + ",inputText=" + inputText);
        if (resid == R.id.bn_10) { // 点击了清除按钮
            clear("");
        } else if (resid == R.id.bn_11) { // 点击了取消按钮
            if (operator.equals("")) { // 无操作符，则表示逐位取消前一个操作数
                if (firstNum.length() > 0) {
                    firstNum = firstNum.substring(0, firstNum.length() - 1);
                } else {
                    operator="";
                    Toast.makeText(this, "没有可取消的数字了", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (sb.toString().contains("\n")){
                    sb.replace(0,sb.length(),sb.substring(0, sb.lastIndexOf("\n")));
                    sb.append("\n").append(firstNum);
                    mTvResult.setText(sb.toString());
                }else {
                    sb.replace(0,sb.length(),firstNum);
                    mTvResult.setText(sb.toString());
                }
            } else { // 有操作符，则表示逐位取消后一个操作数
                if (nextNum.length() == 1) {
                    nextNum = "";
                } else if (nextNum.length() > 0) {
                    nextNum = nextNum.substring(0, nextNum.length() - 1);
                } else {
                    operator="";
                    if (sb.toString().contains("\n")){
                        sb.replace(0,sb.length(),sb.substring(0, sb.lastIndexOf("\n")));
                        sb.append("\n").append(firstNum);
                        mTvResult.setText(sb.toString());
                    }else {
                        sb.replace(0,sb.length(),firstNum);
                        mTvResult.setText(sb.toString());
                    }
                }
                sb.replace(0,sb.length(),sb.substring(0, sb.length()-1));
                mTvResult.setText(sb.toString());
            }
        } else if (resid == R.id.bn_17) { // 点击了等号按钮
            if (operator.length() == 0 || operator.equals("=")) {
                Toast.makeText(this, "请输入运算符", Toast.LENGTH_SHORT).show();
                return;
            } else if (nextNum.length() <= 0) {
                Toast.makeText(this, "请输入数字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (caculate()) { // 计算成功，则显示计算结果
                operator = inputText;
                sb.append("=").append(result);
                mTvResult.setText(sb.toString());
            } else { // 计算失败，则直接返回
                return;
            }
        } else if (resid == R.id.bn_12 // 点击了模、除、乘、减、加按钮
                ||resid == R.id.bn_13 || resid == R.id.bn_14
                || resid == R.id.bn_15 || resid == R.id.bn_16) {
            if (firstNum.length() <= 0) {
                Toast.makeText(this, "请输入数字", Toast.LENGTH_SHORT).show();
                return;
            }
            if (operator.length() == 0 || operator.equals("=")) {
                if (operator.equals("=")){
                    operator = inputText; // 操作符
                    sb.append("\n").append(firstNum).append(operator);
                    mTvResult.setText(sb.toString());
                }else {
                    operator = inputText; // 操作符
                    sb.append(operator);
                    mTvResult.setText(sb.toString());
                }
            } else {
                Toast.makeText(this, "请输入数字", Toast.LENGTH_SHORT).show();
                return;
            }
        } else { // 点击了其它按钮，包括数字和小数点
            if (operator.equals("=")){
                operator="";
                if (firstNum.matches("^-?0+.?0*$")){
                    firstNum = "";
                }
            }
            if (resid == R.id.bn_18) { // 点击了小数点
                if (sb.substring(sb.lastIndexOf("\n")).contains(".")){
                    return;
                }
                inputText = ".";
            }
            if (operator.equals("")) { // 无操作符，则继续拼接前一个操作数
                if (firstNum.equals(result)){
                    firstNum=inputText;
                    result="#";
                    sb.append("\n").append(firstNum);
                    mTvResult.setText(sb.toString());
                    return;
                }else if (first){
                    first=false;
                    firstNum=inputText;
                    sb.append("\n").append(firstNum);
                    mTvResult.setText(sb.toString());
                    return;
                }
                firstNum = firstNum + inputText;
            } else { // 有操作符，则继续拼接后一个操作数
                nextNum = nextNum + inputText;
            }
            sb.append(inputText);
            mTvResult.setText(sb.toString());
        }
    }

    private void initView() {
        mTvResult = (TextView) findViewById(R.id.tv_result);
        // 设置mTvResult内部文本的移动方式为滚动形式
        mTvResult.setMovementMethod(new ScrollingMovementMethod());
        buttons = new Button[19];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = findViewById(getResources().getIdentifier("bn_"+i,"id",getPackageName()));
            buttons[i].setOnClickListener(this);
            buttons[i].setOnKeyListener(this);
        }
    }
    private Button[] buttons;

    private String operator = ""; // 操作符
    private String firstNum = ""; // 前一个操作数
    private String nextNum = ""; // 后一个操作数
    private String result = "#"; // 当前的计算结果
    private static StringBuilder sb = new StringBuilder();// 显示的文本内容


    // 开始加减乘除四则运算，计算成功则返回true，计算失败则返回false
    private boolean caculate() {
        if (operator.equals("+")) { // 当前是相加运算
            result = String.valueOf(Arith.add(firstNum, nextNum));
        } else if (operator.equals("-")) { // 当前是相减运算
            result = String.valueOf(Arith.sub(firstNum, nextNum));
        } else if (operator.equals("×")) { // 当前是相乘运算
            result = String.valueOf(Arith.mul(firstNum, nextNum));
        } else if (operator.equals("÷")) { // 当前是相除运算
            if ("0".equals(nextNum)) { // 发现被除数是0
                // 被除数为0，要弹窗提示用户
                Toast.makeText(this, "被除数不能为零", Toast.LENGTH_SHORT).show();
                // 返回false表示运算失败
                return false;
            } else { // 被除数非0，则进行正常的除法运算
                result = String.valueOf(Arith.div(firstNum, nextNum));
            }
        }else if (operator.equals("%")){    // 当前是取余运算
            result = Arith.mod(firstNum,nextNum);
        }
        // 把运算结果打印到日志中
        Log.d(TAG, "result=" + result);
        firstNum = result;
        nextNum = "";
        // 返回true表示运算成功
        return true;
    }

    // 清空并初始化
    private void clear(String text) {
        sb.replace(0,sb.length(),"");
        mTvResult.setText(sb.toString());
        operator = "";
        firstNum = "";
        nextNum = "";
        result = "";
    }

    private static boolean one = true;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==4)finish();//点击回退键，退出程序
        if (one){
            Log.d(TAG+"####", "keyCode:" + keyCode);
            keyBoardClick(keyCode);
            one=false;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        one=true;
        return false;
    }

    /**
     * 点击键盘执行的方法
     * @param keyCode
     */
    private void keyBoardClick(int keyCode){
        //按下0~9按键时
        if (keyCode>=7&&keyCode<=16){
            keyCode-=7;
            buttons[keyCode].performClick();
            buttons[keyCode].requestFocus();
        }else if (keyCode==31){//按下C键
            buttons[10].performClick();
            buttons[10].requestFocus();
        }else if (keyCode==52/*||keyCode==17||keyCode==59*/){//按下x键或*键
            buttons[14].performClick();
            buttons[14].requestFocus();
        }else if (keyCode==56){//按下.键
            buttons[18].performClick();
            buttons[18].requestFocus();
        }else if (keyCode==67){//按下←键
            buttons[11].performClick();
            buttons[11].requestFocus();
        } else if (keyCode==69||keyCode==156){//按下-键
            buttons[15].performClick();
            buttons[15].requestFocus();
        }else if (keyCode==70){//按下=键或回车键
            buttons[17].performClick();
            buttons[17].requestFocus();
        }else if (keyCode==73){//按下\键, 取余
            buttons[12].performClick();
            buttons[12].requestFocus();
        }else if (keyCode==76){//按下/键
            buttons[13].performClick();
            buttons[13].requestFocus();
        }else if (keyCode==157||keyCode==29){//按下+键或a键
            buttons[16].performClick();
            buttons[16].requestFocus();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(event.getAction()==KeyEvent.ACTION_UP) {
            Log.d(TAG+"11111", "keyCode:" + keyCode);
//            keyBoardClick(keyCode);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            FileOutputStream fos = new FileOutputStream("record.txt");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(sb.toString());
            bw.close();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
