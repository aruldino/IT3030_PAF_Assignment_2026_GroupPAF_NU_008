import { spawn } from 'node:child_process';

const child = spawn(
  '.\\backend\\mvnw.cmd',
  ['-Dmaven.repo.local=.m2repo', '-f', '.\\backend\\pom.xml', 'spring-boot:run'],
  { stdio: 'inherit', shell: true }
);

child.on('exit', (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }
  process.exit(code ?? 0);
});
