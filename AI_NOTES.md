# AI Notes

## 1. Which parts were AI-generated vs. written by me

I used Claude (Anthropic) to generate the initial full implementation — project
structure, model/DTO/repository/service/controller layers, global exception
handling, and both test classes — from the assignment brief.

After that, I:
- I used a DTO to separate the API layer from the entity. It prevents clints from sending fields
    like id , allows request validation, improves security.
- I used simple and correct naming convention so that other developers can understand the code
    and debug the error when issue arises
- I arranged the project structure for maintenance purpose.
- Added validation annotations (@NotBlank, @NotNull, @DecimalMin) to reject invalid input before reaching the business logic.
- Added global exception handling to return meaningful error messages for invalid requests




## 2. What I validated, tested, or changed, and why

- Ran the full test suite (`mvn test`) on a clean checkout to confirm both
  `ExpenseServiceTest` and `ExpenseControllerTest` pass.
- Manually exercised each endpoint with `curl` (see README) to confirm status
  codes (`201` on create, `404` on delete-of-missing-id, `400` on invalid input,
  `204` on successful delete).
- I used BigDecimal for the because monetary values require exact precision.
   double and float can produce rounding errors due to binary floating-point representation
- Verified all REST endpoints using Postman/cURL and confirmed the expected HTTP status codes.
- Ran the test suite and fixed issues until all test cases passed successfully.


## 3. AI suggestions I decided not to use, and why

- The AI offered Docker support and a `/search` endpoint as additional bonus
  options; I chose OpenAPI/Swagger docs instead since the brief said to pick
  at most one, and interactive docs felt most useful for a reviewer testing
  the API by hand.


