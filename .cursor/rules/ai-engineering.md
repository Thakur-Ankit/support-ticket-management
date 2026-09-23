# AI Engineering Guidelines

AI is an engineering assistant, not the final authority.

Before making implementation changes:

1. Read the relevant specification.
2. Read the relevant plan.
3. Read the relevant tasks.
4. Inspect existing code.
5. Make the smallest appropriate change.

Never implement unspecified behavior without explicitly identifying it.

When AI proposes an incorrect approach:

1. Do not blindly accept it.
2. Identify the problem.
3. Correct the implementation.
4. Add a test where appropriate.
5. Record the mistake in docs/ai-mistakes.md.

Do not generate the entire application in one request.

Prefer:

requirement
→ specification
→ plan
→ task
→ implementation
→ test
→ review
→ fix

Every significant AI interaction should be recorded in prompt history.