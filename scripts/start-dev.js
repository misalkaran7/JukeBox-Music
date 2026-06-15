const { execSync, spawn } = require('child_process');
const path = require('path');

const workspaceRoot = path.resolve(__dirname, '..');
const backendWorkingDir = path.join(workspaceRoot, 'jukebox_cli');
const frontendWorkingDir = path.join(workspaceRoot, 'frontend');

const isPortOpen = (port) => {
  if (process.platform !== 'win32') {
    return false;
  }

  try {
    const output = execSync(
      `powershell.exe -NoProfile -Command "Get-NetTCPConnection -LocalPort ${port} -State Listen | Select-Object -First 1 | ForEach-Object { $_.LocalAddress }"`,
      {
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
      }
    );

    return output.trim().length > 0;
  } catch {
    return false;
  }
};

const backendCommand = process.platform === 'win32' ? 'java.exe' : 'java';
const frontendCommand = process.platform === 'win32' ? 'cmd.exe' : 'npm';

const startedChildren = [];

const shutdown = () => {
  for (const child of startedChildren) {
    if (!child.killed) {
      child.kill();
    }
  }
};

async function main() {
  const backendAlreadyRunning = isPortOpen(8091);
  let backend = null;

  if (backendAlreadyRunning) {
    console.log('Backend already listening on http://localhost:8091; reusing it.');
  } else {
    backend = spawn(backendCommand, ['-cp', 'bin', 'jukebox_cli.JukeboxHttpServer'], {
      cwd: backendWorkingDir,
      stdio: 'inherit',
    });
    startedChildren.push(backend);

    backend.on('exit', (code) => {
      if (code && code !== 0) {
        if (!isPortOpen(8091)) {
          console.error(`Backend exited with code ${code}`);
        }
      }
    });
  }

  const frontendAlreadyRunning = isPortOpen(4200);

  if (frontendAlreadyRunning) {
    console.log('Frontend already listening on http://localhost:4200; reusing it.');
  } else {
    const frontendArgs = process.platform === 'win32' ? ['/d', '/s', '/c', 'npm start'] : ['start'];
    const frontend = spawn(frontendCommand, frontendArgs, {
      cwd: frontendWorkingDir,
      stdio: 'inherit',
    });
    startedChildren.push(frontend);

    frontend.on('exit', (code) => {
      shutdown();
      process.exit(code ?? 0);
    });
  }

  if (!backend && frontendAlreadyRunning) {
    process.exit(0);
  }
}

process.on('SIGINT', () => {
  shutdown();
  process.exit(0);
});

process.on('SIGTERM', () => {
  shutdown();
  process.exit(0);
});

main().catch((error) => {
  console.error(error);
  shutdown();
  process.exit(1);
});