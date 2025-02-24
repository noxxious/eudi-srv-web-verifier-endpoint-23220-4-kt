{
  description = "Example EUDIW Verifier backend";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs";
    devshell.url = "github:numtide/devshell";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    devshell,
    flake-utils,
  }:
    {
      overlay = final: prev: {};
    }
    // flake-utils.lib.eachSystem ["aarch64-darwin" "x86_64-darwin" "x86_64-linux"] (system: let
      inherit (nixpkgs) lib;
      pkgs = import nixpkgs {
        inherit system;
        config.allowUnfree = true;
        overlays = [
          devshell.overlays.default
          self.overlay
        ];
      };
    in {
      # Configure your development environment.
      #
      # Documentation: https://github.com/numtide/devshell
      devShell = pkgs.devshell.mkShell {
        name = "eudi-srv-web-verifier-endpoint-23220-4-kt";
        motd = ''
          Entered the EUDIW Verifier backend.
        '';
        env = [
          {
            name = "JAVA_HOME";
            value = pkgs.jdk.home;
          }
        ];
        packages = with pkgs; [
          gradle
          jdk
        ];
      };
    });
}
