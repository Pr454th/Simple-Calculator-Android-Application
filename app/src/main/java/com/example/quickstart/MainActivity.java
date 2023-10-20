package com.example.quickstart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView resultTextView;
    private static final String CHANNEL_ID = "calculator_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.result);
        resultTextView.setText("");
        Button equalButton = findViewById(R.id.equal);
        equalButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                try {
                    pushResultAsNotification();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        });

        Button acButton=findViewById(R.id.ac);
        acButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                resultTextView.setText("");
                return true;
            }
        });
    }

    private void pushResultAsNotification() throws Exception {
        String expression = resultTextView.getText().toString();
        double result = evaluate(expression);

        if (!Double.isNaN(result)) {
            String notificationText = "Result = " + result;

            // Create a notification channel (required for Android 8.0 and higher)
            createNotificationChannel();

            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Calculator Result")
                    .setContentText(notificationText)
                    .setAutoCancel(true);

            // Show the notification
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    // Create a notification channel for Android 8.0 and higher
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Calculator Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Channel for Calculator Notifications");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void onButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        switch (buttonText) {
            case "C/AC":
                String temp=resultTextView.getText().toString();
                if(!temp.isEmpty())
                resultTextView.setText(temp.substring(0,temp.length()-1));
                break;
            case "•":
                resultTextView.setText(resultTextView.getText() + ".");
                break;
            case "\uD83D\uDFF0":
                //evaluate expression
                System.out.println(resultTextView.getText());
                try {
                    double res = evaluate(resultTextView.getText().toString());
                    resultTextView.setText(String.valueOf(res));
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case "➕":
                resultTextView.setText(resultTextView.getText() + "+");
                break;
            case "➖":
                resultTextView.setText(resultTextView.getText() + "-");
                break;
            case "✖️":
                resultTextView.setText(resultTextView.getText() + "*");
                break;
            case "➗":
                resultTextView.setText(resultTextView.getText() + "/");
                break;
            default:
                resultTextView.setText(resultTextView.getText() + buttonText);
                break;
        }
    }

    double evaluate(String expression) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                StringBuilder numBuilder = new StringBuilder();
                while (i < expression.length() && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
                    numBuilder.append(expression.charAt(i));
                    i++;
                }
                i--; // Backtrack one character
                values.push(Double.parseDouble(numBuilder.toString()));
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.pop(); // Pop the '('
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'; // Added '%' as an operator
    }

    private double applyOperator(char operator, double b, double a) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            case '%':
                if (b == 0) {
                    throw new ArithmeticException("Modulo by zero");
                }
                return a % b;
            default:
                return 0;
        }
    }

    int precedence(char operator) {
        if (operator == '+' || operator == '-') {
            return 1;
        } else if (operator == '*' || operator == '/' || operator == '%') {
            return 2;
        }
        return 0;
    }
}