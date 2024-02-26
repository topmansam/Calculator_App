package com.example.calculatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    private TextView display;
    private StringBuilder currentInput = new StringBuilder();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        display = findViewById(R.id.textViewDisplay);

        //Set up listeners for our numbers
        setupNumberButton(findViewById(R.id.button0), "0");
        setupNumberButton(findViewById(R.id.button1), "1");
        setupNumberButton(findViewById(R.id.button2), "2");
        setupNumberButton(findViewById(R.id.button3), "3");
        setupNumberButton(findViewById(R.id.button4), "4");
        setupNumberButton(findViewById(R.id.button5), "5");
        setupNumberButton(findViewById(R.id.button6), "6");
        setupNumberButton(findViewById(R.id.button7), "7");
        setupNumberButton(findViewById(R.id.button8), "8");
        setupNumberButton(findViewById(R.id.button9), "9");

        //Set up listieners for our Operators
        setupOperatorButton(findViewById(R.id.buttonAdd), "+");
        setupOperatorButton(findViewById(R.id.buttonSubtract), "-");
        setupOperatorButton(findViewById(R.id.buttonMultiply), "*");
        setupOperatorButton(findViewById(R.id.buttonDivide), "/");

        findViewById(R.id.buttonAC).setOnClickListener(v -> clearDisplay());
        findViewById(R.id.buttonC).setOnClickListener(v -> correctLastEntry());
        findViewById(R.id.buttonEquals).setOnClickListener(v -> calculateResult());
    }




    //Regoster on click listener for our number
    private void setupNumberButton(Button button, String number) {
        button.setOnClickListener(v -> appendNumber(number));
    }
    //Register on click listener for our operator
    private void setupOperatorButton(Button button, String operator) {
        button.setOnClickListener(v -> appendOperator(operator));
    }

    //Append operator to our stringbuilder
    private void appendOperator(String operator) {
        if(currentInput.length() == 0) {
            // Prevent an operator from being the first character
            return;
        }
        // We should not be able to append 2 operators in a row
        char lastChar = currentInput.charAt(currentInput.length() - 1);
        if (lastChar == '+' || lastChar == '-' || lastChar == '*' || lastChar == '/') {
            // If the last character is an operator, replace it with the new operator
            currentInput.deleteCharAt(currentInput.length() - 1);
        }

        currentInput.append(operator);
        updateDisplay();
    }

    /*
    Append the number to the stringbuilder and update the display.
    We handle the edge case when user inputs 0 at the beginning.
    */
    private void appendNumber(String number) {
        //edge case where user inputs first number as a 0
        if(currentInput.length() == 0 && number.equals("0")) {

            return;
        }
        currentInput.append(number);
        updateDisplay();
    }
    //Remove last chracter when we press 'C'. Esnure our input is greater than 0.
    private void correctLastEntry() {
        if(currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateDisplay();
        }
    }
    //Set the current state of the stringbuilder to the screen.
    private void updateDisplay() {
        display.setText(currentInput.toString());
    }
    private void clearDisplay() {
        currentInput.setLength(0);
        updateDisplay();
    }
    /*
    Using my algorithm from Basic Calculator 2 on leetcode https://leetcode.com/problems/basic-calculator-ii/description/ ...
     To evalaute a expression we have to ensure we are following BEDMAS rules. Multiplication and Divison should have higher precedence over
    Adition and Subtraction. We can solve this by using a Stack and keeping track
    of the last operator seen.
     */
    private void calculateResult() {
        Stack<Integer> stack = new Stack<>();
        char prev_operator = '+';
        int result = 0;
        int currentNumber = 0;
        for(int i = 0; i < currentInput.length(); i++){
            if(Character.isDigit(currentInput.charAt(i))){
                currentNumber = (currentNumber*10) + Character.getNumericValue(currentInput.charAt(i));
            }
            if(!Character.isDigit(currentInput.charAt(i)) && !Character.isWhitespace(currentInput.charAt(i))
                    || i==currentInput.length()-1){
                if(prev_operator =='+'){
                    stack.push(currentNumber);
                }
                else if(prev_operator =='-'){
                    stack.push(-currentNumber);
                }
                else if(prev_operator=='*'){
                    stack.push(stack.pop()*currentNumber);
                }
                //Divide by zero edge case
                else if(prev_operator=='/'){
                    if (currentNumber == 0) {
                        display.setText("Error");
                        return;
                    }
                    stack.push(stack.pop()/currentNumber);
                }
                currentNumber = 0;
                prev_operator =currentInput.charAt(i);
            }
        }
        // Append whatever left on the stack
        while(!stack.isEmpty()){
            result+=stack.pop();
        }

         display.setText(String.valueOf(result));
    }
}