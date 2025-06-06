document.addEventListener('DOMContentLoaded', function() {
    const rows1Input = document.getElementById('rows1');
    const cols1Input = document.getElementById('cols1');
    const rows2Input = document.getElementById('rows2');
    const cols2Input = document.getElementById('cols2');
    const multiplyServerBtn = document.getElementById('multiply-server');
    const generateSendBtn = document.getElementById('generate-send');
    const resultDiv = document.getElementById('result');
    const loadingDiv = document.getElementById('loading');
    
    cols1Input.addEventListener('change', function() {
        rows2Input.value = cols1Input.value;
    });
    
    rows2Input.addEventListener('change', function() {
        cols1Input.value = rows2Input.value;
    });
    
    function displayResult(data, startTime) {
        const endTime = new Date().getTime();
        const executionTime = (endTime - startTime) / 1000; 
        
        resultDiv.textContent = MatrixUtils.formatResult(data, executionTime);
        loadingDiv.style.display = 'none';
    }
    
    function handleError(error) {
        resultDiv.textContent = `Error: ${error.message}`;
        loadingDiv.style.display = 'none';
        console.error('Error:', error);
    }
    
    function validateMatrixSizes() {
        const rows1 = parseInt(rows1Input.value);
        const cols1 = parseInt(cols1Input.value);
        const cols2 = parseInt(cols2Input.value);
        
        if (isNaN(rows1) || isNaN(cols1) || isNaN(cols2) || rows1 < 1 || cols1 < 1 || cols2 < 1) {
            resultDiv.textContent = 'Error: Enter valid matrix dimensions (positive numbers)';
            return null;
        }
        
        return { rows1, cols1, cols2 };
    }
    
    multiplyServerBtn.addEventListener('click', function() {
        const sizes = validateMatrixSizes();
        if (!sizes) return;
        
        loadingDiv.style.display = 'block';
        const startTime = new Date().getTime();
        
        ApiService.multiplyServerMatrices(sizes.rows1, sizes.cols1, sizes.cols2)
            .then(data => displayResult(data, startTime))
            .catch(error => handleError(error));
    });
    
    generateSendBtn.addEventListener('click', function() {
        const sizes = validateMatrixSizes();
        if (!sizes) return;

        const startTime = new Date().getTime();
        
        const matrix1 = MatrixUtils.generateRandomMatrix(sizes.rows1, sizes.cols1);
        const matrix2 = MatrixUtils.generateRandomMatrix(sizes.cols1, sizes.cols2);
        
        loadingDiv.style.display = 'block';
        
        ApiService.multiplyClientMatrices(matrix1, matrix2)
            .then(data => displayResult(data, startTime))
            .catch(error => handleError(error));
    });
});