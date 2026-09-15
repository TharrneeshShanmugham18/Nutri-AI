-- V1__init_schema.sql
-- Milestone M02: Initial Database & Infrastructure Baseline
-- Establishes the core foundation table to verify Flyway migration management
-- and schema evolution without prematurely introducing domain tables.

CREATE TABLE IF NOT EXISTS system_metadata (
    key VARCHAR(64) PRIMARY KEY,
    value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Seed initial infrastructure metadata
INSERT INTO system_metadata (key, value, description)
VALUES 
    ('schema_version', '1.0.0', 'Initial database schema version'),
    ('platform_status', 'INITIALIZED', 'Core database and Flyway foundation initialized')
ON CONFLICT (key) DO NOTHING;

