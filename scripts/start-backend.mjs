import { spawn } from 'node:child_process';

const child = spawn(
  'mvnw.cmd',
  ['-Dmaven.repo.local=.m2repo', 'spring-boot:run'],
  { stdio: 'inherit', shell: true }
);

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 0);
});
