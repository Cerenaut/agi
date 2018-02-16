# AGIEF Notes
The following are additonal notes and gotchas that can be useful when running experiments using AGIEF.

## Image Formatting
The images in a dataset must follow a specific filename format in order to be used in AGIEF experiments. 
The parsing of images is handled by the `ImageLabelEntity`, and the configuration for the parser is in `ImageLabelEntityConfig`.

**Example Format:** `train_0a1e7e_1_616.png`

- 0: train  (dataset prefix)
- 1: 0a1e7e (randomizer)
- 2: 1      (label)
- 3: 616    (sequence)

We have preprocessing tools available for [MNIST](https://github.com/ProjectAGI/Preprocess-MNIST), 
[NIST](https://github.com/ProjectAGI/Preprocess_NIST_SD19) and [SVHN](https://github.com/ProjectAGI/Preprocess-SVHN) 
that convert the datasets into PNG images and produces the required filename format for AGIEF.

## Troubleshooting Docker
If the Dockercontainer has failed, how do you find the problem? Here are some additional information that may be useful for debugging.

Investigate by examining external logs and by SSHing into the machine or EC2 instance for furthur details. The following commands can
be utilised to aid in the debugging process on the machine.

- Use `docker ps` to get running containers

- Use `docker ps -a` to get previous running containers

- Use `docker exec -it [container id] bash` to get into the running container

- Use `docker logs [container id]` to examine the container logs

## Gotchas
These are things you should know when running experiments.

### Using Postgres
If you're using Postgres as a database, `DB_HOST` environment variable needs to be set in the shell environment 
of the Python script, [run-framework](https://github.com/ProjectAGI/run-framework).

If the script is run on one machine, and launches the experiment on AWS or a different machine, then it must be 
set outside the environment by setting the variable in the [variables-docker.sh](https://github.com/ProjectAGI/experiment-definitions/blob/master/variables/variables-docker.sh).
