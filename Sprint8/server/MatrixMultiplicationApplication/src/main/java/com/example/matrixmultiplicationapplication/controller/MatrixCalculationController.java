package com.example.matrixmultiplicationapplication.controller;

import com.example.matrixmultiplicationapplication.dto.ClientMatrixRequest;
import com.example.matrixmultiplicationapplication.dto.ServerMatrixRequest;
import com.example.matrixmultiplicationapplication.model.IntegerMatrix;
import com.example.matrixmultiplicationapplication.service.MatrixCalculationService;
import com.example.matrixmultiplicationapplication.service.StripedParallelMultiplier;
import com.example.matrixmultiplicationapplication.service.StandardMatrixMultiplier;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v1/matrix-operations")
public class MatrixCalculationController {

    private static final int DEFAULT_MAX_STRIPE_SUBDIVISIONS = 4;
    private final Random randomNumberGenerator = new Random();

    @PostMapping("/product/server-generated")
    public ResponseEntity<?> calculateServerGeneratedProduct(@RequestBody ServerMatrixRequest spec) {
        try {
            if (spec == null || spec.getSize1() == null || spec.getSize2() == null ||
                    spec.getSize1().length != 2 || spec.getSize2().length != 2) {
                return ResponseEntity.badRequest().body("Invalid matrix size specifications provided.");
            }

            int rowsA = spec.getSize1()[0];
            int colsA = spec.getSize1()[1];
            int colsB = spec.getSize2()[1];

            if (rowsA <= 0 || colsA <= 0 || colsB <= 0) {
                return ResponseEntity.badRequest().body("Matrix dimensions must be positive.");
            }
            if (colsA != spec.getSize2()[0]) {
                return ResponseEntity.badRequest().body(
                        String.format("Incompatible matrix dimensions for multiplication: Matrix A columns (%d) must equal Matrix B rows (%d).",
                                colsA, spec.getSize2()[0]));
            }

            IntegerMatrix matrixA = new IntegerMatrix(rowsA, colsA, randomNumberGenerator.nextInt(10000));
            IntegerMatrix matrixB = new IntegerMatrix(colsA, colsB, randomNumberGenerator.nextInt(10000));

            MatrixCalculationService parallelCalculator = new StripedParallelMultiplier(DEFAULT_MAX_STRIPE_SUBDIVISIONS);
            IntegerMatrix parallelProduct = parallelCalculator.times(matrixA, matrixB);

            MatrixCalculationService sequentialCalculator = new StandardMatrixMultiplier();
            Instant startTime = Instant.now();
            IntegerMatrix sequentialProduct = sequentialCalculator.times(matrixA, matrixB);
            Instant endTime = Instant.now();
            long sequentialTimeMillis = Duration.between(startTime, endTime).toMillis();

            boolean productsAreEqual = parallelProduct.equals(sequentialProduct);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("sequentialExecutionTimeMillis", sequentialTimeMillis);
            responseBody.put("validationAgainstSequentialCorrect", productsAreEqual);
            responseBody.put("matrixADimensions", rowsA + "x" + colsA);
            responseBody.put("matrixBDimensions", colsA + "x" + colsB);

            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error during matrix operation: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected server error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/product/client-provided")
    public ResponseEntity<?> calculateClientProvidedProduct(@RequestBody ClientMatrixRequest payload) {
        try {
            if (payload == null || payload.getMatrix1() == null || payload.getMatrix2() == null) {
                return ResponseEntity.badRequest().body("Matrix data not provided in request.");
            }

            int[][] dataA = payload.getMatrix1();
            int[][] dataB = payload.getMatrix2();

            IntegerMatrix matrixA = new IntegerMatrix(dataA);
            IntegerMatrix matrixB = new IntegerMatrix(dataB);

            if (matrixA.getColumnCount() != matrixB.getRowCount()) {
                return ResponseEntity.badRequest().body(
                        String.format("Incompatible matrix dimensions for multiplication: Matrix A columns (%d) must equal Matrix B rows (%d).",
                                matrixA.getColumnCount(), matrixB.getRowCount()));
            }

            MatrixCalculationService parallelCalculator = new StripedParallelMultiplier(DEFAULT_MAX_STRIPE_SUBDIVISIONS);
            IntegerMatrix parallelProduct = parallelCalculator.times(matrixA, matrixB);

            MatrixCalculationService sequentialCalculator = new StandardMatrixMultiplier();
            Instant startTime = Instant.now();
            IntegerMatrix sequentialProduct = sequentialCalculator.times(matrixA, matrixB);
            Instant endTime = Instant.now();
            long sequentialTimeMillis = Duration.between(startTime, endTime).toMillis();

            boolean productsAreEqual = parallelProduct.equals(sequentialProduct);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("sequentialExecutionTimeMillis", sequentialTimeMillis);
            responseBody.put("validationAgainstSequentialCorrect", productsAreEqual);

            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error processing client matrices: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An unexpected server error occurred: " + e.getMessage());
        }
    }
}