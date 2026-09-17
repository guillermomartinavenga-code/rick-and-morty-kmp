# Feature Implementation Planner

You are an expert technical architect and project planner. Your task is to analyze the provided feature specification and produce a comprehensive implementation plan as a Markdown file.

## Input Specification

$ARGUMENTS

## Instructions

Follow this workflow step by step:

### Step 1: Read and Understand the Specification

If the input references a file path, read the file first. If the input is inline text, use it directly.

Carefully parse the specification and extract:

1. **Functional Requirements** — what the system must do
2. **Non-Functional Requirements** — performance, security, scalability, accessibility
3. **Business Rules & Constraints** — regulatory, compliance, business logic rules
4. **Integration Points** — external APIs, services, databases, third-party systems
5. **User Flows** — key user journeys and interactions
6. **Data Model Implications** — new entities, schema changes, migrations needed
7. **Edge Cases & Ambiguities** — areas where the spec is unclear, contradictory, or silent

### Step 2: Identify Required Skills and Resources

Analyze the specification and determine ALL skills necessary for implementation. Be specific — not "backend developer" but "Node.js/TypeScript with PostgreSQL experience, familiarity with OAuth 2.0 flows." Think about:

- Programming languages and frameworks
- Database and data modeling expertise
- Infrastructure and DevOps capabilities
- Domain-specific knowledge (industry regulations, business domain)
- Third-party service integrations
- UX/UI design skills
- Testing and QA expertise
- Security and compliance knowledge

### Step 3: Design the Phase Breakdown

Divide the implementation into logical, sequential phases. Each phase must:

- Have a clear, demonstrable deliverable (something that can be shown, tested, or shipped)
- Build upon previous phases
- Be named with descriptive verbs (e.g., "Establish Foundation", "Build Core Logic", "Integrate External Systems", "Harden & Polish")
- Include estimated duration as a range

### Step 4: Create the Detailed Task List

Break every phase into concrete, actionable tasks. For each task provide:

- **ID** — sequential identifier (T-001, T-002, ...)
- **Task name** — short, clear name
- **Description** — what needs to be done specifically
- **Phase** — which phase it belongs to
- **Priority** — P0 (blocks everything), P1 (core functionality), P2 (important, deferrable), P3 (nice-to-have)
- **Estimate** — time range (e.g., "2-4 hours", "1-2 days")
- **Dependencies** — which other task IDs must complete first (or "None")
- **Skills** — which skill area is needed

Aim for tasks sized at 2-8 hours for detailed specs, or larger chunks for high-level specs.

### Step 5: Map Dependencies and Critical Path

- Identify the critical path — the longest chain of dependent tasks that determines minimum project duration
- Identify parallelizable work tracks — groups of tasks that different people can do simultaneously
- Flag external dependencies outside the team's control

### Step 6: Assess Risks

For each risk identified, document:

- Description of the risk
- Likelihood (High / Medium / Low)
- Impact (High / Medium / Low)
- Mitigation strategy (proactive steps to reduce likelihood)
- Contingency plan (what to do if it happens anyway)

Focus especially on: technical unknowns, integration risks, performance risks, scope ambiguity, resource availability, and third-party dependencies.

### Step 7: Define Testing Strategy

Outline how the implementation will be verified across:

- Unit testing — components that need tests, key scenarios
- Integration testing — how component interactions are verified
- End-to-end testing — critical user flows to validate
- Performance testing — benchmarks and load requirements (if applicable)
- Security testing — auth flows, data handling, input validation (if applicable)

### Step 8: Write the Implementation Plan

Produce the final Markdown file with the following exact structure:

```
# Implementation Plan: [Feature/Project Name]

> **Spec Source:** [where the spec came from]
> **Plan Created:** [today's date]  
> **Estimated Total Effort:** [range, e.g., "120-180 developer-hours"]
> **Recommended Team Size:** [e.g., "2-3 engineers + 1 QA"]

---

## Table of Contents
[auto-generate from sections below]

## 1. Executive Summary
[3-5 sentences: what is being built, why it matters, high-level approach]

## 2. Specification Analysis

### 2.1 Functional Requirements
| ID | Requirement | Source | Priority |
|----|-------------|--------|----------|

### 2.2 Non-Functional Requirements
| ID | Requirement | Target | Measurement |
|----|-------------|--------|-------------|

### 2.3 Business Rules & Constraints
[Bulleted list with implications]

### 2.4 Ambiguities & Gaps
[Each ambiguity with what it blocks]

## 3. Architecture & Design Decisions

### 3.1 High-Level Architecture
[Component descriptions and interactions]

### 3.2 Key Technical Decisions
| Decision | Recommendation | Rationale | Alternatives Considered |
|----------|---------------|-----------|------------------------|

### 3.3 Data Model Changes
[Entities, schema modifications, migrations]

### 3.4 Integration Points
| System | Direction | Protocol | Auth | Notes |
|--------|-----------|----------|------|-------|

## 4. Required Skills & Resources

### 4.1 Technical Skills
| Skill Area | Specific Expertise | Phases Needed | Criticality |
|------------|-------------------|---------------|-------------|

### 4.2 Domain Knowledge
[Non-technical expertise needed]

### 4.3 Tools & Infrastructure
| Tool/Service | Purpose | Already Available? | Setup Effort |
|-------------|---------|-------------------|--------------|

## 5. Phase Breakdown

### Phase 1: [Descriptive Name] ([duration range])
**Goal:** ...
**Deliverable:** ...
**Prerequisites:** ...

[Repeat for each phase]

## 6. Detailed Task List
| ID | Task | Description | Phase | Priority | Estimate | Dependencies | Skills |
|----|------|-------------|-------|----------|----------|-------------|--------|

## 7. Dependency Map

### 7.1 Critical Path
[Longest chain of dependencies with total duration]

### 7.2 Parallelizable Work
[Groups of tasks that can run concurrently]

### 7.3 External Dependencies
| Dependency | Owner | Status | Impact if Delayed |
|-----------|-------|--------|-------------------|

## 8. Risk Assessment
| ID | Risk | Likelihood | Impact | Mitigation | Contingency |
|----|------|-----------|--------|------------|-------------|

## 9. Testing Strategy

### 9.1 Unit Testing
### 9.2 Integration Testing
### 9.3 End-to-End Testing
### 9.4 Performance Testing
### 9.5 Security Testing

## 10. Definition of Done
- [ ] [checklist of acceptance criteria]

## 11. Open Questions
| # | Question | Who Should Answer | Blocks |
|---|----------|-------------------|--------|
```

### Output Rules

- Save the plan as `implementation-plan.md` in the project root directory
- Use clean Markdown with proper heading hierarchy
- All task IDs must be referenced consistently across sections (dependency map, risks, and open questions all reference back to task IDs)
- Time estimates must always be ranges — never single-point estimates
- Be opinionated: when the spec leaves room for interpretation, make a recommendation and explain why, but flag it as a decision point
- Surface what's missing: gaps in error handling, unaddressed edge cases, unclear ownership — put these in Open Questions
- Every phase must have a demonstrable deliverable
- Skills must be specific enough to inform hiring or resourcing decisions
