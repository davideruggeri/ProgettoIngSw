const fs = require('fs');
const pdf = require('pdf-parse');

let dataBuffer = fs.readFileSync('C:/App/ProgettoIngSw/Elaborato2025_26_def.pdf');

pdf(dataBuffer).then(function (data) {
    fs.writeFileSync('C:/App/ProgettoIngSw/pdf_text.txt', data.text);
    console.log("PDF text extracted successfully.");
}).catch(function (error) {
    console.error("Error extracting PDF text:", error);
});
