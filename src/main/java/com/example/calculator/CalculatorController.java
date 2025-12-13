package com.example.calculator;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;

/**
 * Controller class that handles calculator operations.
 * Uses session to maintain calculator state.
 */
@Controller
public class CalculatorController {

	/**
	 * Serves the main calculator page.
	 */
	@GetMapping("/")
	public String index() {
		return "index.html";
	}

	/**
	 * Get current calculator state
	 */
	@GetMapping("/state")
	@ResponseBody
	public CalculatorResponse getState(HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
	}

	/**
	 * Input a number
	 */
	@PostMapping("/input")
	@ResponseBody
	public CalculatorResponse inputNumber(@RequestParam("value") String value, HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		
		try {
			if (state.isNewNumber()) {
				state.setDisplay(value);
				state.setCurrentNumber(Double.parseDouble(value));
				state.setNewNumber(false);
			} else {
				String newDisplay = state.getDisplay() + value;
				state.setDisplay(newDisplay);
				state.setCurrentNumber(Double.parseDouble(newDisplay));
			}
		} catch (NumberFormatException e) {
			return new CalculatorResponse(state.getDisplay(), state.getExpression(), "Invalid number");
		}
		
		return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
	}

	/**
	 * Input decimal point
	 */
	@PostMapping("/decimal")
	@ResponseBody
	public CalculatorResponse inputDecimal(HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		
		if (state.isNewNumber()) {
			state.setDisplay("0.");
			state.setNewNumber(false);
		} else if (!state.getDisplay().contains(".")) {
			state.setDisplay(state.getDisplay() + ".");
		}
		
		return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
	}

	/**
	 * Set operation
	 */
	@PostMapping("/operation")
	@ResponseBody
	public CalculatorResponse setOperation(@RequestParam("op") String operation, HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		
		// If we have a pending operation, calculate it first
		if (state.getPreviousNumber() != 0 && state.getOperation() != null && !state.isNewNumber()) {
			CalculatorResponse calcResult = performCalculation(state);
			if (calcResult.getError() != null) {
				return calcResult;
			}
		}
		
		// Set up new operation
		state.setPreviousNumber(state.getCurrentNumber());
		state.setOperation(operation);
		state.setNewNumber(true);
		
		return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
	}

	/**
	 * Calculate result
	 */
	@PostMapping("/equals")
	@ResponseBody
	public CalculatorResponse calculate(HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		return performCalculation(state);
	}

	/**
	 * Clear all
	 */
	@PostMapping("/clear")
	@ResponseBody
	public CalculatorResponse clearAll(HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		state.clearAll();
		return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
	}

	/**
	 * Clear entry
	 */
	@PostMapping("/clearEntry")
	@ResponseBody
	public CalculatorResponse clearEntry(HttpSession session) {
		CalculatorState state = getCalculatorState(session);
		state.clearEntry();
		return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
	}

	private CalculatorState getCalculatorState(HttpSession session) {
		CalculatorState state = (CalculatorState) session.getAttribute("calculatorState");
		if (state == null) {
			state = new CalculatorState();
			session.setAttribute("calculatorState", state);
		}
		return state;
	}

	private CalculatorResponse performCalculation(CalculatorState state) {
		if (state.getOperation() == null) {
			return new CalculatorResponse(state.getDisplay(), state.getExpression(), null);
		}

		try {
			double result = 0;
			double num1 = state.getPreviousNumber();
			double num2 = state.getCurrentNumber();

			switch (state.getOperation()) {
				case "add":
					result = num1 + num2;
					break;
				case "subtract":
					result = num1 - num2;
					break;
				case "multiply":
					result = num1 * num2;
					break;
				case "divide":
					if (num2 == 0) {
						return new CalculatorResponse(state.getDisplay(), state.getExpression(), "Division by zero");
					}
					result = num1 / num2;
					break;
				default:
					return new CalculatorResponse(state.getDisplay(), state.getExpression(), "Invalid operation");
			}

			state.setCurrentNumber(result);
			state.setPreviousNumber(0);
			state.setOperation(null);
			state.setNewNumber(true);

		} catch (Exception e) {
			return new CalculatorResponse(state.getDisplay(), state.getExpression(), e.getMessage());
		}

		return new CalculatorResponse(state.getDisplay(), "", null);
	}
	
	/**
	 * Response class for calculator operations
	 */
	public static class CalculatorResponse {
		private String display;
		private String expression;
		private String error;
		
		public CalculatorResponse(String display, String expression, String error) {
			this.display = display;
			this.expression = expression;
			this.error = error;
		}
		
		public String getDisplay() {
			return display;
		}
		
		public String getExpression() {
			return expression;
		}
		
		public String getError() {
			return error;
		}
	}
}