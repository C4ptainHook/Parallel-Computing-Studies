class ApiService {
    static multiplyServerMatrices(rows1, cols1, cols2) {
        return fetch(`${CONFIG.SERVER_URL}${CONFIG.ENDPOINTS.MULTIPLY_SERVER}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                size1: [rows1, cols1],
                size2: [cols1, cols2]
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Server error');
            }
            return response.json();
        });
    }

    static multiplyClientMatrices(matrix1, matrix2) {
        return fetch(`${CONFIG.SERVER_URL}${CONFIG.ENDPOINTS.MULTIPLY_CLIENT}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                matrix1: matrix1,
                matrix2: matrix2
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Server error');
            }
            return response.json();
        });
    }
}