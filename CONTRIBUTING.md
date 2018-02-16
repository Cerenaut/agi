# Contributing Guide
The following is a set of guidelines for contributing to AGIEF that aim to provide the necessary information for new contributers to help them get started.

## Documentation
- [AGIEF Wiki](https://github.com/ProjectAGI/agi/wiki)
- [Project AGI Website](https://agi.io)
- [Technical Documentation](./docs)

## What should I know before I get started?

### System Architecture
Reading the [wiki](https://github.com/ProjectAGI/agi/wiki) and the [documentation](./docs) should give you a good understanding of how the framework works, and how different system components interact with each other. This will be particularly useful in debugging any issues you encounter, and for making valuable contributions to the codebase.
   
### Coding Conventions
The coding conventions for this codebase can be found [here](https://github.com/ProjectAGI/agi/wiki/Coding-Conventions). To make it easier for contributers to follow the coding guidelines, we provide a coding format for the IntelliJ IDEA development environment which can be found [here](./resources/code-format).

### Development Environment
Setting up a local development environment is necessary for advanced users or contributers who wish to submit bug fixes or improvements. The [technical documentation](./docs) contains the necessary information for setting up a local development environment, as well as information about system components and additional notes.

## How Can I Contribute?

### Reporting Bugs
This section guides you through submitting a bug report for AGIEF. Following these guidelines helps maintainers and the community understand your bug report, reproduce the behavior, and find related reports.

#### Before Submitting A Bug Report
* Check the [documentation](./docs). The documentation includes information that can help you debug the problem and fix things yourself, or it may include some tips to address common issues.
* Determine which repository the problem should be reported in.
* Perform a [cursory search](https://github.com/search?utf8=%E2%9C%93&q=+is%3Aissue+user%3Aprojectagi) to see if the problem has already been reported. If it has and the issue is still open, add a comment to the existing issue instead of opening a new one.

#### How Do I Submit A Good Bug Report?
Bug reports are tracked as [GitHub issues](https://github.com/ProjectAGI/agi/issues). After you've determined which repository your enhancement suggestion is related to, create an issue on that repository and provide the following information:

* Use a clear and descriptive title for the issue to identify the problem.
* Describe the exact steps which reproduce the problem in as many details as possible.
* Provide specific examples to demonstrate the steps. Include links to files or GitHub projects, or copy/pasteable snippets, which you use in those examples. If you're providing snippets in the issue, use Markdown code blocks.
* Describe the behavior you observed after following the steps and point out what exactly is the problem with that behavior.
* Explain which behavior you expected to see instead and why.
* Include screenshots which show you following the described steps and clearly demonstrate the problem.
* If the problem is related to performance or memory, include a CPU profile capture with your report.
* If the problem wasn't triggered by a specific action, describe what you were doing before the problem happened and share more information using the guidelines below.

1. Read the [documentation](./docs) which may address known issues
2. Search GitHub Issues for similar bugs if the documentation does not cover your issue
3. If the bug is not reported, start by [creating a new issue](https://github.com/ProjectAGI/agi/issues/new) on GitHub
4. Provide the necessary details in order to reproduce your bug along with relevant system information
5. Label the issue appropriately as a `bug`

### Suggesting Enhancements
This section will guide you through submitting an enhancement suggestion for AGIEF. This includes new features and minor improvements to existing functionality.

#### Before Submitting An Enhancement Suggestion
* Determine which [repository](https://github.com/ProjectAGI) the enhancement should be suggested in.
* Perform a [cursory search](https://github.com/search?utf8=%E2%9C%93&q=+is%3Aissue+user%3Ate a new one.

#### How Do I Submit A Good Enhancement Suggestion?
Enhancement suggestions are tracked as [GitHub issues](https://github.com/ProjectAGI/agi/issues). After you've determined which repository your enhancement suggestion is related to, create an issue on that repository and provide the following information:

* Use a clear and descriptive title for the issue to identify the suggestion.
* Provide a step-by-step description of the suggested enhancement in as many details as possible.
* Provide specific examples to demonstrate the steps. Include copy/pasteable snippets which you use in those examples, as Markdown code blocks.
* Describe the current behavior and explain which behavior you expected to see instead and why.
* Include screenshots which can help you demonstrate the steps or point out the part of AGIEF which the suggestion is related to.
* Explain why this enhancement would be useful to most users.
* List some other frameworks where this enhancement exists.
* Specify the name and version of the operating system you're using.

### Code Contributions
To start contributing to the codebase, ensure that you have the appropriate local development environment and adhere to the [coding guidelines](https://github.com/ProjectAGI/agi/wiki/Coding-Conventions). The development environment can be setup by following the instructions in the [documentation](./docs).

#### Pull Requests
* Fork the repository and perform the necessary code changes
* Create a new pull request on this repository to merge your changes
* The pull request should contain the following details:
    * Description of the change
    * Alternative designs
    * Benefits of the change
    * Potential drawbacks
    * Verification process
