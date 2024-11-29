const { exec } = require("child_process");
const fs = require("fs");
const path = require("path");

// Function to execute shell commands without displaying output
const runCommand = (command, options = {}) => {
  return new Promise((resolve, reject) => {
    const process = exec(command, options, (error, stdout, stderr) => {
      if (error) {
        console.error(`Error executing: ${command}`);
        console.error(`Error message: ${error.message}`);
        console.error(`Stderr: ${stderr}`);
        return reject(error);
      }
      // Suppress stdout and stderr in normal cases
      resolve(stdout);
    });
  });
};

// Function to recursively build dependencies
const buildDependencies = async (dependencies, baseDir) => {
  for (const { url, branch } of dependencies) {
    try {
      console.log(`Processing repository: ${url} (${branch})`);

      // Extract the repo name from the URL
      const repoName = url.split("/").pop().replace(".git", "");

      // Clone the repository
      console.log(`Cloning ${url}...`);
      const repoPath = path.join(baseDir, repoName);
      await runCommand(`git clone ${url} ${repoPath}`);

      // Change directory to the cloned repo
      process.chdir(repoPath);

      // Checkout the specified branch
      console.log(`Checking out branch ${branch}...`);
      await runCommand(`git checkout ${branch}`);

      // Check for dependencies.json in the cloned repo
      const dependenciesFilePath = path.join(repoPath, "dependencies.json");
      if (fs.existsSync(dependenciesFilePath)) {
        console.log(`Found dependencies.json in ${repoName}. Resolving dependencies...`);
        const nestedDependencies = JSON.parse(fs.readFileSync(dependenciesFilePath, "utf-8"));
        await buildDependencies(nestedDependencies, baseDir); // Use baseDir for nested dependencies
      }

      // Run the build script
      console.log(`Running build script for ${repoName}...`);
      await runCommand(`bash buildScript.sh`);

      // Return to the original directory
      process.chdir(baseDir);

      console.log(`Successfully built ${repoName}!\n`);
    } catch (error) {
      console.error(`Failed to process ${url}: ${error.message}\n`);
    }
  }
};

// Main function to initiate the process
const main = async () => {
  const baseDir = path.resolve(__dirname); // Start in the current directory

  try {
    // Clone repo0, which contains the initial dependencies.json
    const dependenciesFilePath = path.join(baseDir, "dependencies.json");
    if (!fs.existsSync(dependenciesFilePath)) {
      console.error("No dependencies.json found in repo0.");
      process.exit(1);
    }

    // Read and process the initial dependencies.json
    console.log("Reading dependencies.json from repo0...");
    const dependencies = JSON.parse(fs.readFileSync(dependenciesFilePath, "utf-8"));
    await buildDependencies(dependencies, baseDir);
  } catch (error) {
    console.error(`Failed to process repo0: ${error.message}`);
  }
};

// Execute the script
main()
  .then(() => console.log("All dependencies built successfully!"))
  .catch((error) => console.error(`Script failed: ${error.message}`));
