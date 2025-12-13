package com.example.calculator;

/**
 * Calculator state class to maintain calculation session
 */
public class CalculatorState {
    private double currentNumber = 0;
    private double previousNumber = 0;
    private String operation = null;
    private boolean isNewNumber = true;
    private String display = "0";
    private String expression = "";
    
    public double getCurrentNumber() {
        return currentNumber;
    }
    
    public void setCurrentNumber(double currentNumber) {
        this.currentNumber = currentNumber;
        this.display = formatNumber(currentNumber);
    }
    
    public double getPreviousNumber() {
        return previousNumber;
    }
    
    public void setPreviousNumber(double previousNumber) {
        this.previousNumber = previousNumber;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
        updateExpression();
    }
    
    public boolean isNewNumber() {
        return isNewNumber;
    }
    
    public void setNewNumber(boolean newNumber) {
        isNewNumber = newNumber;
    }
    
    public String getDisplay() {
        return display;
    }
    
    public void setDisplay(String display) {
        this.display = display;
    }
    
    public String getExpression() {
        return expression;
    }
    
    public void clearAll() {
        currentNumber = 0;
        previousNumber = 0;
        operation = null;
        isNewNumber = true;
        display = "0";
        expression = "";
    }
    
    public void clearEntry() {
        currentNumber = 0;
        isNewNumber = true;
        display = "0";
    }
    
    private String formatNumber(double number) {
        if (number == (long) number) {
            return String.valueOf((long) number);
        } else {
            return String.valueOf(number);
        }
    }
    
    private void updateExpression() {
        if (operation != null && previousNumber != 0) {
            String operationSymbol = getOperationSymbol(operation);
            expression = formatNumber(previousNumber) + " " + operationSymbol;
        } else {
            expression = "";
        }
    }
    
    private String getOperationSymbol(String op) {
        switch (op) {
            case "add": return "+";
            case "subtract": return "−";
            case "multiply": return "×";
            case "divide": return "÷";
            default: return "";
        }
    }
}