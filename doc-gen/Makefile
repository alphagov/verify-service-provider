.PHONY: requirements
requirements:
	npm install
	bundle install

.PHONY: html
html:
	$(if ${API_SPEC},,$(error Must specify path to the OpenAPI spec file in API_SPEC))
	./node_modules/widdershins/widdershins.js -c -u templates/ -y ${API_SPEC} -o source/index.html.md
	bundle exec middleman build

.PHONY: server
server:
	bundle exec middleman server

