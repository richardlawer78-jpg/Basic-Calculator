const buttonsEl = document.querySelectorAll("button");
const inputEl = document.getElementById("result");

for (let i = 0; i < buttonsEl.length; i++) {
  buttonsEl[i].addEventListener("click", function () {
    const buttonValue = this.textContent;
    if (buttonValue === "C") {
      clearResult();
    } else if (buttonValue === "=") {
      calculateResult();
    } else {
      appendToResult(buttonValue);
    }
  });
}

function clearResult() {
  inputEl.value = "";
}

function calculateResult() {
  try {
    inputEl.value = eval(inputEl.value);
  } catch (error) {
    inputEl.value = "Error";
  }
}

function appendToResult(value) {
  inputEl.value += value;
}
