#!/usr/bin/env python3
import subprocess
import sys
import shutil

def find_java():
    """Locate a java executable on PATH."""
    java = shutil.which("java")
    if not java:
        sys.exit("ERROR: 'java' not found in PATH. Install or update your JAVA_HOME.")
    return java

def run_jar(jar_path: str, selections: list[str]):
    """
    Runs the given JAR, feeds it the selections list (each as one line),
    and streams its stdout/stderr back to us.
    """
    java = find_java()
    cmd = [java, "-jar", jar_path]

    # Open the process
    proc = subprocess.Popen(
        cmd,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,         # use strings, not bytes
        bufsize=1,         # line-buffered
    )

    # Prepare the input: each selection followed by newline
    user_input = "\n".join(selections) + "\n"

    # Communicate our input, capture output
    out, err = proc.communicate(input=user_input)

    # Print what the jar printed
    if out:
        print("=== STDOUT ===")
        print(out)
    if err:
        print("=== STDERR ===", file=sys.stderr)
        print(err, file=sys.stderr)

    return proc.returncode

def main():
    if len(sys.argv) < 3:
        print(f"Usage: {sys.argv[0]} <jar-file> <option1> [option2] …")
        print("Example: ")
        print(f"  {sys.argv[0]} batelco-migration-1.0-SNAPSHOT-jar-with-dependencies.jar 2")
        print("  # runs the jar and selects option #2 at the menu")
        sys.exit(1)

    jar = sys.argv[1]
    selections = sys.argv[2:]
    code = run_jar(jar, selections)
    sys.exit(code)

if __name__ == "__main__":
    main()


# use case: python3 run_migration.py batelco-migration-1.0-SNAPSHOT-jar-with-dependencies.jar 2
# say for option 2.