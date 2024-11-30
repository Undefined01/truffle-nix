{
  description = "Nix development dependencies for unic-project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, ... } @ inputs:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
        };
      in {
        devShell = pkgs.mkShell {
          packages = with pkgs; [
            gcc
            gnumake
            graalvm-ce
            gradle
            maven
            tree-sitter

            clang-tools
            jdt-language-server
          ];
        };
      }
    );
}
