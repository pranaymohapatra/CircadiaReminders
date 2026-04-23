-- Create custom enum types
CREATE TYPE task_status AS ENUM ('PAUSED', 'ACTIVE', 'COMPLETED');
CREATE TYPE reminder_type AS ENUM ('STATIC', 'DYNAMIC');
CREATE TYPE trigger_event AS ENUM ('AWAKE', 'LOCATION', 'MANUAL');
CREATE TYPE user_provider AS ENUM ('LOCAL', 'GOOGLE');

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    provider user_provider NOT NULL DEFAULT 'LOCAL',
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL, -- This maps to the User UUID string
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    revoked       BOOLEAN      NOT NULL    DEFAULT FALSE,
    date_created  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create projects table
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(7), -- HEX color code
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create reminders table with single flat structure
CREATE TABLE reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status task_status NOT NULL DEFAULT 'PAUSED',
    due_date TIMESTAMPTZ,
    context_tags TEXT[], -- PostgreSQL array for tags
    priority INTEGER DEFAULT 0, -- 0=low, 1=medium, 2=high
    is_recurring BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Reminder type and execution fields (single flat structure)
    reminder_type reminder_type NOT NULL DEFAULT 'STATIC',
    execution_time TIME, -- For STATIC reminders (e.g., 14:00)
    days_of_week INTEGER[],
    interval_minutes INTEGER, -- For DYNAMIC reminders
    trigger_event trigger_event, -- For DYNAMIC reminders
    
    -- Flat EndCondition columns
    end_condition_type VARCHAR(10), -- Store the Enum name: "TIME", "EVENT", or "MANUAL"
    end_condition_value VARCHAR(255) -- Store the actual time or event name as a string
);

-- Create indexes for better performance
CREATE INDEX idx_projects_user_id ON projects(user_id);
CREATE INDEX idx_projects_is_archived ON projects(is_archived);

-- Foreign Key and Global lookup indexes
CREATE INDEX idx_reminders_project_id ON reminders(project_id);
CREATE INDEX idx_reminders_due_date ON reminders(due_date);

-- Composite indexes for user-specific queries
CREATE INDEX idx_reminders_user_status ON reminders(user_id, status);
CREATE INDEX idx_reminders_user_project ON reminders(user_id, project_id);
CREATE INDEX idx_reminders_user_due_date ON reminders(user_id, due_date);
CREATE INDEX idx_reminders_user_type ON reminders(user_id, reminder_type);
CREATE INDEX idx_reminders_user_priority ON reminders(user_id, priority);

-- Performance indexes for scheduler/trigger lookups
CREATE INDEX idx_reminders_execution_time ON reminders(execution_time);
CREATE INDEX idx_reminders_trigger_event ON reminders(trigger_event);

-- GIN indexes for array types
CREATE INDEX idx_reminders_context_tags ON reminders USING GIN(context_tags);
CREATE INDEX idx_reminders_days_of_week ON reminders USING GIN(days_of_week);

-- Create trigger function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_projects_updated_at BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reminders_updated_at BEFORE UPDATE ON reminders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
