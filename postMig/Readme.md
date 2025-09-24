mkdir -p ~/post_mig_suite && cd ~/post_mig_suite
chmod +x post_*.sh revert_*.sh run_all.sh

# Run the full post-migration set:
./run_all.sh post

# Run the full revert set:
./run_all.sh revert

# Run a subset (e.g., only 05..08 of post):
START=5 END=8 ./run_all.sh post
All logs go to ./logs/ with timestamps.
