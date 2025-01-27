{
  description = "doughnut development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-24.11";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            allowUnfree = true;
            permittedInsecurePackages = [];
          };
          overlays = [
            (final: prev: {
              boost = prev.boost.override {
                enableShared = true;
                enableStatic = true;
                extraB2Flags = [ "--without-stacktrace" ];
              };
            })
          ];
        };

        inherit (pkgs) stdenv lib;
        apple_sdk = pkgs.darwin.apple_sdk.frameworks;
      in {
        devShells.default = pkgs.mkShell {
          name = "doughnut";

          buildInputs = with pkgs;
            [
              jdk23
              nodejs_23
              corepack_23
              python312
              poetry
              zsh
              git
              git-secret
              gitleaks
              jq
              libmysqlclient
              mysql80
              mysql-client
              mysql_jdbc
              process-compose
              yamllint
              nixfmt-classic
              hclfmt
              fzf
            ] ++ lib.optionals stdenv.isDarwin [ sequelpro ]
            ++ lib.optionals (!stdenv.isDarwin) [
              sequeler
              ungoogled-chromium
              psmisc
              x11vnc
              xclip
              xvfb-run
            ];

          shellHook = ''
            # Make script compatible with both bash and zsh
            # Set core environment variables
            if [ -n "''${ZSH_VERSION:-}" ]; then
              emulate -L bash
              setopt pipefail
              export PS1="(nix)''${PS1:-%# }"
            else
              set -euo pipefail
              export PS1="(nix)''${PS1:-$ }"
            fi

            # Configure fzf
            export FZF_DEFAULT_OPTS="--height 40% --layout=reverse --border"
            if [ -n "''${ZSH_VERSION:-}" ]; then
              if [ -e "${pkgs.fzf}/share/fzf/key-bindings.zsh" ]; then
                source ${pkgs.fzf}/share/fzf/key-bindings.zsh
              fi
              if [ -e "${pkgs.fzf}/share/fzf/completion.zsh" ]; then
                source ${pkgs.fzf}/share/fzf/completion.zsh
              fi
            else
              if [ -e "${pkgs.fzf}/share/fzf/key-bindings.bash" ]; then
                source ${pkgs.fzf}/share/fzf/key-bindings.bash
              fi
              if [ -e "${pkgs.fzf}/share/fzf/completion.bash" ]; then
                source ${pkgs.fzf}/share/fzf/completion.bash
              fi
            fi

            # Define and export logging function
            log() {
              echo "[$(date +'%Y-%m-%d %H:%M:%S')] $*"
            }
            export -f log

            # Add git push script alias
            alias g='./scripts/git_push.sh'

            # Deactivate nvm if exists
            command -v nvm >/dev/null 2>&1 && { nvm deactivate; }

            # General settings
            export LANG="en_US.UTF-8"
            export SOURCE_REPO_NAME="''${PWD##*/}"

            # Export core paths first
            export JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"
            export NODE_PATH="$(dirname $(dirname $(readlink -f $(which node))))"
            export PNPM_HOME="$(dirname $(dirname $(readlink -f $(which pnpm))))"
            export PYTHON_PATH="$(dirname $(dirname $(readlink -f $(which python))))"
            export POETRY_PATH="$(dirname $(dirname $(readlink -f $(which poetry))))"
            export PATH=$JAVA_HOME/bin:$NODE_PATH/bin:$PNPM_HOME/bin:$PATH

            # Export MySQL configuration
            export MYSQL_BASEDIR="${pkgs.mysql80}"
            export MYSQL_HOME="''${PWD}/mysql"
            export MYSQL_DATADIR="''${MYSQL_HOME}/data"
            export MYSQL_UNIX_SOCKET="''${MYSQL_HOME}/mysql.sock"
            export MYSQLX_UNIX_SOCKET="''${MYSQL_HOME}/mysqlx.sock"
            export MYSQL_PID_FILE="''${MYSQL_HOME}/mysql.pid"
            export MYSQL_TCP_PORT="3309"
            export MYSQLX_TCP_PORT="33090"
            export MYSQL_LOG_FILE="''${MYSQL_HOME}/mysql.log"

            cat << 'EOF'
            ╔════════════════════════════════════════════════════════════════════════════════════╗
            ║                         NIX DEVELOPMENT ENVIRONMENT                                ║
            ╚════════════════════════════════════════════════════════════════════════════════════╝
            EOF

            printf "\n%s\n" "🚀 Project: $SOURCE_REPO_NAME"
            printf "📦 Versions:\n"
            printf "  • Nix:    %s\n" "$(nix --version)"
            printf "  • Java:   %s\n" "$(javac --version)"
            printf "  • Node:   %s\n" "$(node --version)"
            printf "  • PNPM:   %s\n" "$(pnpm --version)"
            printf "  • Biome:  %s\n" "$(pnpm biome --version)"
            printf "  • Python: %s\n" "$(python --version)"
            printf "  • Poetry: %s\n" "$(poetry --version)"

            printf "\n📂 Paths:\n"
            printf "  • JAVA_HOME:     %s\n" "$JAVA_HOME"
            printf "  • NODE_PATH:     %s\n" "$NODE_PATH"
            printf "  • PNPM_HOME:     %s\n" "$PNPM_HOME"
            printf "  • PYTHON_PATH:   %s\n" "$PYTHON_PATH"
            printf "  • MYSQL_HOME:    %s\n" "$MYSQL_HOME"
            printf "  • MYSQL_DATADIR: %s\n" "$MYSQL_DATADIR"
            printf "\n"

            log "Environment setup complete! 🎉"

            # Start process-compose and wait for it to be ready
            (
              mkdir -p "$MYSQL_HOME"
              process-compose up -f process-compose.yaml --detached >/dev/null 2>&1
            )

            return 0
          '';
        };
      });
}
