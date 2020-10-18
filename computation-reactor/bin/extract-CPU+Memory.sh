#extract CPU and Memory Usage from top results file $1
set -o errexit -o nounset

ex - "$1" <<-EOF
% s/^.*S//g
% s/^.*R//g
% s/...:.*$//g
w
EOF
