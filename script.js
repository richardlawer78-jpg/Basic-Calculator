const buttonsEl = document.querySelectorAll("button");
const inputEl = document.getElementById("result");

// Calculator state
let currentInput = "";
let operator = null;
let previousInput = "";
let shouldResetDisplay = false;

// Add event listeners to all buttons
for (let i = 0; i < buttonsEl.length; i++) {
  buttonsEl[i].addEventListener("click", function () {
    const buttonValue = this.textContent;
    const buttonClass = this.className;
    
    if (buttonClass.includes("clear")) {
      clearResult();
    } else if (buttonClass.includes("number") || buttonClass.includes("decimal")) {
      appendNumber(buttonValue);
    } else if (buttonClass.includes("operator")) {
      handleOperator(buttonValue);
    } else if (buttonClass.includes("equals")) {
      calculateResult();
    } else if (buttonClass.includes("percent")) {
      handlePercentage();
    }
  });
}

// Keyboard support
document.addEventListener("keydown", function(event) {
  const key = event.key;
  
  if (key >= "0" && key <= "9" || key === ".") {
    appendNumber(key);
  } else if (key === "+" || key === "-" || key === "*" || key === "/") {
    handleOperator(key);
  } else if (key === "Enter" || key === "=") {
    calculateResult();
  } else if (key === "Escape" || key === "c" || key === "C") {
    clearResult();
  } else if (key === "Backspace") {
    backspace();
  }
});

function clearResult() {
  inputEl.value = "";
  currentInput = "";
  operator = null;
  previousInput = "";
  shouldResetDisplay = false;
}

function appendNumber(number) {
  if (shouldResetDisplay) {
    inputEl.value = "";
    shouldResetDisplay = false;
  }
  
  if (number === "." && inputEl.value.includes(".")) {
    return; // Prevent multiple decimal points
  }
  
  inputEl.value += number;
  currentInput = inputEl.value;
}

function handleOperator(op) {
  if (inputEl.value === "") return;
  
  if (operator !== null && previousInput !== "") {
    calculateResult();
  }
  
  operator = op;
  previousInput = inputEl.value;
  shouldResetDisplay = true;
}

function calculateResult() {
  if (operator === null || previousInput === "" || currentInput === "") {
    return;
  }
  
  try {
    const prev = parseFloat(previousInput);
    const current = parseFloat(currentInput);
    let result;
    
    switch (operator) {
      case "+":
        result = prev + current;
        break;
      case "-":
        result = prev - current;
        break;
      case "*":
        result = prev * current;
        break;
      case "/":
        if (current === 0) {
          inputEl.value = "Error: Division by zero";
          return;
        }
        result = prev / current;
        break;
      default:
        return;
    }
    
    // Round to avoid floating point precision issues
    result = Math.round(result * 100000000) / 100000000;
    inputEl.value = result.toString();
    currentInput = result.toString();
    operator = null;
    previousInput = "";
    shouldResetDisplay = true;
    
  } catch (error) {
    inputEl.value = "Error";
  }
}

function handlePercentage() {
  if (inputEl.value === "") return;
  
  try {
    const value = parseFloat(inputEl.value);
    const result = value / 100;
    inputEl.value = result.toString();
    currentInput = result.toString();
  } catch (error) {
    inputEl.value = "Error";
  }
}

function backspace() {
  if (inputEl.value.length > 0) {
    inputEl.value = inputEl.value.slice(0, -1);
    currentInput = inputEl.value;
  }
}
