# ============================================
# Runtime-only image (pre-built locally)
# ============================================
# npmjs.org is blocked on Iranian servers.
# Build locally with: npm run build
FROM nginx:alpine

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy pre-built dist files
COPY dist/ /usr/share/nginx/html/ca978112ca/

# Expose nginx port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget -q --spider http://localhost/ca978112ca/ || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
