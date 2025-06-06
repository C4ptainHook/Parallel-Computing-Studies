class MatrixUtils {
    static generateRandomMatrix(rows, cols) {
        const matrix = [];
        for (let i = 0; i < rows; i++) {
            const row = [];
            for (let j = 0; j < cols; j++) {
                row.push(Math.floor(Math.random() * 10));
            }
            matrix.push(row);
        }
        return matrix;
    }

    static canMultiply(cols1, rows2) {
        return cols1 === rows2;
    }

    static formatResult(data, executionTime) {
        const correctnessIcon = data.validationAgainstSequentialCorrect ? "✓" : "✗";
        const statusText = data.validationAgainstSequentialCorrect ? "Valid" : "Invalid";
        
        return `MATRIX MULTIPLICATION COMPLETED

⏱️  Execution Time: ${executionTime.toFixed(3)} seconds

${correctnessIcon} Validation: ${statusText}
   ${data.validationAgainstSequentialCorrect ? 
    'Results verified against sequential computation' : 
    'Results differ from expected sequential output'}

📊 Operation completed successfully`;
    }
}