import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';
import eslintConfigPrettier from 'eslint-config-prettier';

/** @type {import('eslint').Linter.FlatConfig[]} */
export default [
    {
        ignores: ['node_modules', 'dist', 'build', 'coverage'],
        languageOptions: {
            ecmaVersion: 'latest',
            sourceType: 'module',
            globals: {
                window: 'readonly',
                document: 'readonly',
                navigator: 'readonly',
                localStorage: 'readonly',
                sessionStorage: 'readonly',
                console: 'readonly',
                setTimeout: 'readonly',
                clearTimeout: 'readonly',
                setInterval: 'readonly',
                clearInterval: 'readonly',
            },
        },
    },
    ...pluginVue.configs['flat/recommended'],
    js.configs.recommended,
    // Override rules after recommended configs
    {
        rules: {
            'no-unused-vars': 'off',
            'vue/no-unused-vars': 'off',
        }
    },
    // Keep this last to disable rules that conflict with Prettier
    eslintConfigPrettier,
];
