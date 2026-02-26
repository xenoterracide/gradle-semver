// SPDX-FileCopyrightText: Copyright © 2026 Caleb Cushing
//
// SPDX-License-Identifier: MIT

const prettier = "prettier --cache --ignore-unknown --write";
const reuse = "reuse annotate";
const copyright = "--copyright 'Caleb Cushing' --merge-copyrights";
const symbol = "--copyright-prefix spdx-string-symbol";
const hashComment = "--style python";

const licenseCode = "--license 'GPL-3.0-or-later'";
const licenseConfiguration = "--license 'CC0-1.0' --fallback-dot-license";
const licenseDocumentation = "--license 'CC-BY-NC-4.0";
const licenseScripts = "--license 'MIT' --fallback-dot-license";

const withoutYarn = (files) => files.filter((file) => !file.includes("/.yarn/") && !file.startsWith(".yarn/"));

const withFiles = (command, files) => `${command} ${files.map((file) => `"${file.replace(/"/g, '\\"')}"`).join(" ")}`;

const run = (commands) => (files) => {
  const filtered = withoutYarn(files);

  if (!filtered.length) {
    return [];
  }

  const cmds = Array.isArray(commands) ? commands : [commands];
  return cmds.map((command) => withFiles(command, filtered));
};

module.exports = {
  // should change per repo
  "*.{ts,java}": run([`${reuse} ${copyright} ${symbol} ${licenseCode}`, prettier]),

  "!(package).json": run([`${reuse} ${copyright} ${symbol} ${licenseConfiguration}`, prettier]),
  "package.json": run([`${reuse} ${copyright} ${symbol} ${licenseScripts}`, prettier]),

  Makefile: run([`${reuse} ${copyright} ${symbol} ${licenseScripts}`]),
  "{.config/git/hooks/**,**/*.sh}": run([`${reuse} ${copyright} ${symbol} ${licenseScripts} --style python`, prettier]),
  "*.{md,adoc}": run([`${reuse} ${copyright} ${symbol} ${licenseDocumentation}`, prettier]),
  "*.{xml,yaml,properties,toml,json5,js}": run([`${reuse} ${copyright} ${licenseConfiguration} ${symbol}`, prettier]),
  "*.{js,cjs,yml}": run([`${reuse} ${copyright} ${symbol} ${licenseScripts}`, prettier]),
  ".{*ignore,editorconfig,gitattributes,mailmap}": run([
    `${reuse} ${copyright} ${symbol} ${licenseConfiguration}`,
    prettier,
  ]),
  "*.properties": run([`${reuse} ${copyright} ${licenseConfiguration}`, prettier]),
};
