--
-- PostgreSQL database dump
--

\restrict EMUAt7G9P9penhpF7PaOWuBv4rCJkLUSuQobHreK27gvMnJTIZPxxi40UlKqhnR

-- Dumped from database version 15.17
-- Dumped by pg_dump version 15.17

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: ad_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ad_requests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    message_content character varying(10000),
    message_media_url character varying(255),
    message_type character varying(255),
    reviewed_at timestamp(6) with time zone,
    reviewed_by uuid,
    source_id character varying(255),
    source_message_id character varying(255),
    source_type character varying(255),
    status character varying(20) NOT NULL,
    requester_id uuid,
    target_channel_id uuid,
    CONSTRAINT ad_requests_message_type_check CHECK (((message_type)::text = ANY ((ARRAY['TEXT'::character varying, 'IMAGE'::character varying, 'VIDEO'::character varying, 'VIDEO_NOTE'::character varying, 'VOICE'::character varying, 'AUDIO'::character varying, 'FILE'::character varying, 'LOCATION'::character varying, 'CONTACT'::character varying, 'STICKER'::character varying, 'GIF'::character varying, 'POLL'::character varying, 'LINK'::character varying])::text[]))),
    CONSTRAINT ad_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.ad_requests OWNER TO postgres;

--
-- Name: ai_bot_messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ai_bot_messages (
    id uuid NOT NULL,
    bot_id uuid NOT NULL,
    content text NOT NULL,
    created_at timestamp(6) with time zone,
    role character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    action_label character varying(255),
    action_url character varying(255)
);


ALTER TABLE public.ai_bot_messages OWNER TO postgres;

--
-- Name: ai_bots; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ai_bots (
    id uuid NOT NULL,
    avatar_url character varying(255),
    bot_type character varying(255),
    category character varying(255),
    created_at timestamp(6) with time zone,
    description character varying(500),
    display_order integer NOT NULL,
    is_active boolean NOT NULL,
    name character varying(255)
);


ALTER TABLE public.ai_bots OWNER TO postgres;

--
-- Name: channel_post_amplitudes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_post_amplitudes (
    channel_post_id uuid NOT NULL,
    amplitudes integer
);


ALTER TABLE public.channel_post_amplitudes OWNER TO postgres;

--
-- Name: channel_post_comments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_post_comments (
    id uuid NOT NULL,
    content character varying(2000),
    created_at timestamp(6) with time zone,
    post_id uuid,
    user_id uuid
);


ALTER TABLE public.channel_post_comments OWNER TO postgres;

--
-- Name: channel_post_reactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_post_reactions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    reaction character varying(255),
    post_id uuid,
    user_id uuid
);


ALTER TABLE public.channel_post_reactions OWNER TO postgres;

--
-- Name: channel_posts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_posts (
    id uuid NOT NULL,
    ad_label character varying(255),
    ad_source_channel_id uuid,
    comments_enabled boolean NOT NULL,
    content character varying(10000),
    created_at timestamp(6) with time zone,
    edited_at timestamp(6) with time zone,
    forwarded_from character varying(255),
    is_ad boolean NOT NULL,
    is_pinned boolean NOT NULL,
    media_url character varying(255),
    pinned_at timestamp(6) with time zone,
    pinned_by_id uuid,
    scheduled_at timestamp(6) with time zone,
    type character varying(255),
    view_count integer NOT NULL,
    channel_id uuid,
    poll_id uuid,
    action_label character varying(255),
    action_url character varying(255),
    timer_target_at timestamp(6) with time zone,
    CONSTRAINT channel_posts_type_check CHECK (((type)::text = ANY (ARRAY['TEXT'::text, 'IMAGE'::text, 'VIDEO'::text, 'VIDEO_NOTE'::text, 'VOICE'::text, 'AUDIO'::text, 'FILE'::text, 'LOCATION'::text, 'CONTACT'::text, 'STICKER'::text, 'GIF'::text, 'POLL'::text, 'LINK'::text])))
);


ALTER TABLE public.channel_posts OWNER TO postgres;

--
-- Name: channel_subscribers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channel_subscribers (
    id uuid NOT NULL,
    can_add_members boolean NOT NULL,
    can_edit_info boolean NOT NULL,
    can_post_story boolean NOT NULL,
    can_remove_members boolean NOT NULL,
    is_admin boolean NOT NULL,
    is_archived boolean NOT NULL,
    is_mandatory boolean DEFAULT false NOT NULL,
    is_muted boolean NOT NULL,
    is_pinned boolean NOT NULL,
    subscribed_at timestamp(6) with time zone,
    channel_id uuid,
    user_id uuid
);


ALTER TABLE public.channel_subscribers OWNER TO postgres;

--
-- Name: channels; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.channels (
    id uuid NOT NULL,
    avatar_url character varying(255),
    classification character varying(30) NOT NULL,
    created_at timestamp(6) with time zone,
    description character varying(1000),
    display_mode character varying(20) NOT NULL,
    institution_id uuid,
    invite_link character varying(255),
    is_official boolean NOT NULL,
    is_public boolean NOT NULL,
    is_verified_teacher boolean NOT NULL,
    name character varying(255),
    official_category character varying(255),
    public_id character varying(255),
    target_city character varying(255),
    target_education_level character varying(255),
    target_field_of_study character varying(255),
    target_province character varying(255),
    target_university character varying(255),
    owner_id uuid,
    target_ministry character varying(255),
    target_audience_type character varying(255),
    CONSTRAINT channels_classification_check CHECK (((classification)::text = ANY ((ARRAY['GENERAL'::character varying, 'VERIFIED_TEACHER'::character varying, 'ELM_CLUB_INSTITUTION'::character varying, 'COURSE_CHANNEL'::character varying, 'HASHTAG_NATIONAL'::character varying, 'HASHTAG_UNIVERSITY'::character varying, 'HASHTAG_BRANCH'::character varying])::text[]))),
    CONSTRAINT channels_display_mode_check CHECK (((display_mode)::text = ANY ((ARRAY['SPECIAL'::character varying, 'TAB'::character varying, 'SUPPORT'::character varying])::text[]))),
    CONSTRAINT channels_official_category_check CHECK (((official_category)::text = ANY ((ARRAY['STUDENTS_IRAN'::character varying, 'MY_FIELD'::character varying, 'MY_UNIVERSITY'::character varying, 'MY_UNION'::character varying, 'FREELANCING'::character varying, 'PODCAST'::character varying, 'JOURNAL'::character varying, 'RESEARCH'::character varying, 'COMPETITIONS'::character varying, 'SCIENCE_TECH'::character varying, 'EDUCATION'::character varying, 'STUDENT_NEWS'::character varying, 'ENTERTAINMENT'::character varying, 'APP_OFFICIAL'::character varying, 'LOTTERY_DISCOUNT'::character varying])::text[])))
);


ALTER TABLE public.channels OWNER TO postgres;

--
-- Name: chat_participants; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chat_participants (
    chat_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.chat_participants OWNER TO postgres;

--
-- Name: chats; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chats (
    id uuid NOT NULL,
    avatar_url character varying(255),
    created_at timestamp(6) with time zone,
    is_archived boolean NOT NULL,
    is_muted boolean NOT NULL,
    is_pinned boolean NOT NULL,
    title character varying(255),
    type character varying(255),
    updated_at timestamp(6) with time zone,
    CONSTRAINT chats_type_check CHECK (((type)::text = ANY ((ARRAY['PRIVATE'::character varying, 'GROUP'::character varying, 'CHANNEL'::character varying])::text[])))
);


ALTER TABLE public.chats OWNER TO postgres;

--
-- Name: clubs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.clubs (
    id uuid NOT NULL,
    display_order integer NOT NULL,
    name character varying(255)
);


ALTER TABLE public.clubs OWNER TO postgres;

--
-- Name: collaboration_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.collaboration_requests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    message character varying(2000),
    responded_at timestamp(6) with time zone,
    status character varying(255),
    title character varying(255),
    receiver_id uuid,
    sender_id uuid,
    CONSTRAINT collaboration_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.collaboration_requests OWNER TO postgres;

--
-- Name: content_purchases; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.content_purchases (
    id uuid NOT NULL,
    expires_at timestamp(6) with time zone,
    purchased_at timestamp(6) with time zone,
    content_id uuid,
    transaction_id uuid,
    user_id uuid
);


ALTER TABLE public.content_purchases OWNER TO postgres;

--
-- Name: course_admins; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_admins (
    course_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.course_admins OWNER TO postgres;

--
-- Name: course_chapters; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_chapters (
    course_id uuid NOT NULL,
    duration_text character varying(255),
    title character varying(255),
    session_start_time timestamp(6) with time zone,
    session_end_time timestamp(6) with time zone
);


ALTER TABLE public.course_chapters OWNER TO postgres;

--
-- Name: course_collaboration_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_collaboration_requests (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    message character varying(255),
    sender_institution_id uuid,
    status character varying(255),
    target_institution_id uuid,
    updated_at timestamp(6) with time zone,
    course_id uuid,
    CONSTRAINT course_collaboration_requests_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


ALTER TABLE public.course_collaboration_requests OWNER TO postgres;

--
-- Name: course_collaborators; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_collaborators (
    course_id uuid NOT NULL,
    collaborator_id character varying(255)
);


ALTER TABLE public.course_collaborators OWNER TO postgres;

--
-- Name: course_comments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_comments (
    id uuid NOT NULL,
    content text,
    created_at timestamp(6) with time zone,
    rating integer NOT NULL,
    reply_to_comment_id uuid,
    course_id uuid,
    user_id uuid
);


ALTER TABLE public.course_comments OWNER TO postgres;

--
-- Name: course_enrollments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_enrollments (
    id uuid NOT NULL,
    enrolled_at timestamp(6) with time zone,
    is_active boolean NOT NULL,
    course_id uuid,
    user_id uuid,
    reminder_sent boolean NOT NULL
);


ALTER TABLE public.course_enrollments OWNER TO postgres;

--
-- Name: course_manual_instructors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_manual_instructors (
    course_id uuid NOT NULL,
    avatar_url character varying(255),
    name character varying(255),
    resume character varying(255)
);


ALTER TABLE public.course_manual_instructors OWNER TO postgres;

--
-- Name: course_materials; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_materials (
    id uuid NOT NULL,
    content_type character varying(255),
    content_url character varying(255),
    created_at timestamp(6) with time zone,
    description text,
    is_locked boolean NOT NULL,
    sort_order integer NOT NULL,
    title character varying(255),
    course_id uuid
);


ALTER TABLE public.course_materials OWNER TO postgres;

--
-- Name: course_suitable_for; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_suitable_for (
    course_id uuid NOT NULL,
    audience character varying(255)
);


ALTER TABLE public.course_suitable_for OWNER TO postgres;

--
-- Name: course_tags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_tags (
    course_id uuid NOT NULL,
    tag character varying(255)
);


ALTER TABLE public.course_tags OWNER TO postgres;

--
-- Name: course_teachers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.course_teachers (
    course_id uuid NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE public.course_teachers OWNER TO postgres;

--
-- Name: courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.courses (
    id uuid NOT NULL,
    admin_note text,
    average_rating double precision NOT NULL,
    capacity integer,
    cover_image_url character varying(255),
    created_at timestamp(6) with time zone,
    description text,
    discount_percentage integer NOT NULL,
    education_level character varying(255),
    ends_at timestamp(6) with time zone,
    enrollment_limit integer,
    favorites_count integer NOT NULL,
    field_of_study character varying(255),
    institution_id uuid,
    is_public boolean NOT NULL,
    is_vertical_poster boolean NOT NULL,
    organizer_description text,
    price_rials bigint NOT NULL,
    review_count integer NOT NULL,
    scientific_association_name character varying(255),
    slogan character varying(300),
    starts_at timestamp(6) with time zone,
    status character varying(255),
    syllabus_duration character varying(255),
    title character varying(500),
    updated_at timestamp(6) with time zone,
    channel_id uuid,
    group_id uuid,
    organizer_id uuid,
    bbb_attendee_password character varying(255),
    bbb_meeting_id character varying(255),
    bbb_moderator_password character varying(255),
    CONSTRAINT courses_status_check CHECK (((status)::text = ANY (ARRAY['DRAFT'::text, 'PENDING'::text, 'APPROVED'::text, 'REJECTED'::text, 'ACTIVE'::text, 'COMPLETED'::text, 'CANCELLED'::text])))
);


ALTER TABLE public.courses OWNER TO postgres;

--
-- Name: discounts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.discounts (
    id uuid NOT NULL,
    brand_name character varying(255),
    category character varying(255),
    code character varying(255),
    created_at timestamp(6) with time zone,
    description character varying(255),
    expiry_date date,
    image_url character varying(255),
    percent integer NOT NULL,
    title character varying(255)
);


ALTER TABLE public.discounts OWNER TO postgres;

--
-- Name: education_levels; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.education_levels (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    display_order integer NOT NULL,
    has_faculty boolean DEFAULT false NOT NULL,
    has_field_of_study boolean DEFAULT false NOT NULL,
    name character varying(255) NOT NULL,
    role_value_en character varying(255)
);


ALTER TABLE public.education_levels OWNER TO postgres;

--
-- Name: educational_role_options; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.educational_role_options (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    display_order integer NOT NULL,
    emoji character varying(255),
    label_fa character varying(255) NOT NULL,
    value_en character varying(255) NOT NULL
);


ALTER TABLE public.educational_role_options OWNER TO postgres;

--
-- Name: elm_events; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.elm_events (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    date character varying(255),
    description text,
    image_url character varying(255),
    is_approved boolean NOT NULL,
    is_external boolean NOT NULL,
    link character varying(255),
    location character varying(255),
    organizer character varying(255),
    reward character varying(255),
    submitted_by_user_id uuid,
    title character varying(255) NOT NULL,
    type character varying(255),
    CONSTRAINT elm_events_type_check CHECK (((type)::text = ANY ((ARRAY['COMPETITION'::character varying, 'STARTUP'::character varying, 'CONGRESS'::character varying])::text[])))
);


ALTER TABLE public.elm_events OWNER TO postgres;

--
-- Name: entertainment_movies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entertainment_movies (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    description character varying(2000),
    duration character varying(255),
    is_active boolean NOT NULL,
    release_date character varying(255),
    thumbnail_url character varying(255),
    title character varying(255),
    video_url character varying(255)
);


ALTER TABLE public.entertainment_movies OWNER TO postgres;

--
-- Name: entertainment_music; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entertainment_music (
    id uuid NOT NULL,
    artist character varying(255),
    audio_url character varying(255),
    cover_url character varying(255),
    created_at timestamp(6) with time zone,
    duration character varying(255),
    is_active boolean NOT NULL,
    title character varying(255)
);


ALTER TABLE public.entertainment_music OWNER TO postgres;

--
-- Name: entertainment_riddles; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entertainment_riddles (
    id uuid NOT NULL,
    correct_answer_index integer,
    created_at timestamp(6) with time zone,
    description character varying(2000),
    is_active boolean NOT NULL,
    is_multiple_choice boolean NOT NULL,
    question character varying(2000),
    reward character varying(255),
    title character varying(255),
    type character varying(255)
);


ALTER TABLE public.entertainment_riddles OWNER TO postgres;

--
-- Name: event_reports; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.event_reports (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    date character varying(255),
    description text,
    is_verified boolean NOT NULL,
    link character varying(255),
    location character varying(255),
    points_awarded integer NOT NULL,
    title character varying(255) NOT NULL,
    user_id uuid
);


ALTER TABLE public.event_reports OWNER TO postgres;

--
-- Name: exam_access_rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_access_rules (
    id uuid NOT NULL,
    channel_id uuid,
    rule_type character varying(255),
    user_id uuid,
    exam_id uuid
);


ALTER TABLE public.exam_access_rules OWNER TO postgres;

--
-- Name: exam_answers; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_answers (
    id uuid NOT NULL,
    answer_text text,
    graded_at timestamp(6) with time zone,
    graded_by uuid,
    is_correct boolean,
    score numeric(10,2),
    selected_option character varying(5),
    attempt_id uuid,
    question_id uuid
);


ALTER TABLE public.exam_answers OWNER TO postgres;

--
-- Name: exam_attempts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_attempts (
    id uuid NOT NULL,
    auto_score numeric(10,2),
    device_info character varying(500),
    duration_seconds integer,
    final_score numeric(10,2),
    ip_address character varying(45),
    is_submitted boolean NOT NULL,
    manual_score numeric(10,2),
    started_at timestamp(6) with time zone,
    submitted_at timestamp(6) with time zone,
    exam_id uuid,
    user_id uuid
);


ALTER TABLE public.exam_attempts OWNER TO postgres;

--
-- Name: exam_question_options; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_question_options (
    id uuid NOT NULL,
    is_correct boolean NOT NULL,
    option_label character varying(5),
    option_text text,
    sort_order integer NOT NULL,
    question_id uuid
);


ALTER TABLE public.exam_question_options OWNER TO postgres;

--
-- Name: exam_questions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exam_questions (
    id uuid NOT NULL,
    correct_answer text,
    created_at timestamp(6) with time zone,
    image_url character varying(255),
    points numeric(10,2),
    question_text text,
    question_type character varying(255),
    sort_order integer NOT NULL,
    exam_id uuid,
    CONSTRAINT exam_questions_question_type_check CHECK (((question_type)::text = ANY ((ARRAY['MULTIPLE_CHOICE'::character varying, 'FILL_BLANK'::character varying, 'SHORT_ANSWER'::character varying, 'DESCRIPTIVE'::character varying, 'IMAGE_BASED'::character varying])::text[])))
);


ALTER TABLE public.exam_questions OWNER TO postgres;

--
-- Name: exams; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.exams (
    id uuid NOT NULL,
    channel_id uuid,
    course_id uuid,
    created_at timestamp(6) with time zone,
    description text,
    duration_minutes integer NOT NULL,
    ends_at timestamp(6) with time zone,
    is_public boolean NOT NULL,
    max_attempts integer NOT NULL,
    pass_score numeric(10,2),
    show_results_after boolean NOT NULL,
    shuffle_options boolean NOT NULL,
    shuffle_questions boolean NOT NULL,
    starts_at timestamp(6) with time zone,
    status character varying(255),
    title character varying(500),
    total_score numeric(10,2),
    updated_at timestamp(6) with time zone,
    creator_id uuid,
    CONSTRAINT exams_status_check CHECK (((status)::text = ANY ((ARRAY['DRAFT'::character varying, 'SCHEDULED'::character varying, 'ACTIVE'::character varying, 'ENDED'::character varying, 'GRADED'::character varying])::text[])))
);


ALTER TABLE public.exams OWNER TO postgres;

--
-- Name: faculties; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.faculties (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    display_order integer NOT NULL,
    education_level character varying(255),
    name character varying(255) NOT NULL
);


ALTER TABLE public.faculties OWNER TO postgres;

--
-- Name: feedbacks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feedbacks (
    id uuid NOT NULL,
    admin_note character varying(2000),
    created_at timestamp(6) with time zone,
    description character varying(2000) NOT NULL,
    rating integer NOT NULL,
    status character varying(20) NOT NULL,
    title character varying(255) NOT NULL,
    user_id uuid,
    CONSTRAINT feedbacks_status_check CHECK (((status)::text = ANY ((ARRAY['OPEN'::character varying, 'IN_PROGRESS'::character varying, 'RESOLVED'::character varying, 'CLOSED'::character varying])::text[])))
);


ALTER TABLE public.feedbacks OWNER TO postgres;

--
-- Name: fields_of_study; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.fields_of_study (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    display_order integer NOT NULL,
    education_level character varying(255) NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE public.fields_of_study OWNER TO postgres;

--
-- Name: group_members; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_members (
    id uuid NOT NULL,
    can_add_members boolean NOT NULL,
    can_edit_info boolean NOT NULL,
    can_post_story boolean NOT NULL,
    can_remove_members boolean NOT NULL,
    is_archived boolean NOT NULL,
    is_mandatory boolean NOT NULL,
    is_muted boolean NOT NULL,
    is_pinned boolean NOT NULL,
    joined_at timestamp(6) with time zone,
    role character varying(255),
    group_id uuid,
    user_id uuid,
    CONSTRAINT group_members_role_check CHECK (((role)::text = ANY ((ARRAY['OWNER'::character varying, 'ADMIN'::character varying, 'MEMBER'::character varying])::text[])))
);


ALTER TABLE public.group_members OWNER TO postgres;

--
-- Name: group_message_amplitudes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_message_amplitudes (
    group_message_id uuid NOT NULL,
    amplitudes integer
);


ALTER TABLE public.group_message_amplitudes OWNER TO postgres;

--
-- Name: group_message_reactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_message_reactions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    reaction character varying(255),
    message_id uuid,
    user_id uuid
);


ALTER TABLE public.group_message_reactions OWNER TO postgres;

--
-- Name: group_messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.group_messages (
    id uuid NOT NULL,
    content character varying(10000),
    created_at timestamp(6) with time zone,
    edited_at timestamp(6) with time zone,
    forwarded_from character varying(255),
    is_edited boolean NOT NULL,
    is_pinned boolean NOT NULL,
    media_url character varying(255),
    pinned_at timestamp(6) with time zone,
    pinned_by_id uuid,
    scheduled_at timestamp(6) with time zone,
    type character varying(255),
    group_id uuid,
    poll_id uuid,
    reply_to_id uuid,
    sender_id uuid,
    action_label character varying(255),
    action_url character varying(255),
    timer_target_at timestamp(6) with time zone,
    CONSTRAINT group_messages_type_check CHECK (((type)::text = ANY (ARRAY['TEXT'::text, 'IMAGE'::text, 'VIDEO'::text, 'VIDEO_NOTE'::text, 'VOICE'::text, 'AUDIO'::text, 'FILE'::text, 'LOCATION'::text, 'CONTACT'::text, 'STICKER'::text, 'GIF'::text, 'POLL'::text, 'LINK'::text])))
);


ALTER TABLE public.group_messages OWNER TO postgres;

--
-- Name: groups; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.groups (
    id uuid NOT NULL,
    allow_members_to_edit_info boolean NOT NULL,
    allow_members_to_send_messages boolean NOT NULL,
    avatar_url character varying(255),
    created_at timestamp(6) with time zone,
    description character varying(1000),
    display_mode character varying(20) NOT NULL,
    hide_members boolean NOT NULL,
    invite_link character varying(255),
    is_invite_link_enabled boolean NOT NULL,
    is_official boolean NOT NULL,
    is_public boolean NOT NULL,
    name character varying(255),
    official_category character varying(255),
    target_city character varying(255),
    target_education_level character varying(255),
    target_field_of_study character varying(255),
    target_province character varying(255),
    target_university character varying(255),
    created_by uuid,
    target_ministry character varying(255),
    target_audience_type character varying(255),
    CONSTRAINT groups_display_mode_check CHECK (((display_mode)::text = ANY ((ARRAY['SPECIAL'::character varying, 'TAB'::character varying, 'SUPPORT'::character varying])::text[])))
);


ALTER TABLE public.groups OWNER TO postgres;

--
-- Name: hashtag_promotions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hashtag_promotions (
    id uuid NOT NULL,
    content_text text,
    created_at timestamp(6) with time zone,
    moderated_at timestamp(6) with time zone,
    moderated_by uuid,
    moderation_status character varying(255),
    published_at timestamp(6) with time zone,
    rejection_reason character varying(500),
    subscription_id uuid,
    hashtag_id uuid,
    user_id uuid,
    CONSTRAINT hashtag_promotions_moderation_status_check CHECK (((moderation_status)::text = ANY ((ARRAY['PENDING'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.hashtag_promotions OWNER TO postgres;

--
-- Name: home_banners; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.home_banners (
    id uuid NOT NULL,
    color_end bigint NOT NULL,
    color_start bigint NOT NULL,
    created_at timestamp(6) with time zone,
    display_order integer NOT NULL,
    image_url character varying(255),
    is_active boolean NOT NULL,
    link_url character varying(255),
    section character varying(255),
    title character varying(255)
);


ALTER TABLE public.home_banners OWNER TO postgres;

--
-- Name: institution_admins; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_admins (
    institution_id uuid NOT NULL,
    admin_id uuid
);


ALTER TABLE public.institution_admins OWNER TO postgres;

--
-- Name: institution_clubs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_clubs (
    institution_id uuid NOT NULL,
    club_id character varying(255)
);


ALTER TABLE public.institution_clubs OWNER TO postgres;

--
-- Name: institution_faculties; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_faculties (
    institution_id uuid NOT NULL,
    faculty_name character varying(255)
);


ALTER TABLE public.institution_faculties OWNER TO postgres;

--
-- Name: institution_fields; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_fields (
    institution_id uuid NOT NULL,
    field_id character varying(255)
);


ALTER TABLE public.institution_fields OWNER TO postgres;

--
-- Name: institution_honors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_honors (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    date date,
    description character varying(1000),
    image_url character varying(255),
    title character varying(255),
    institution_id uuid
);


ALTER TABLE public.institution_honors OWNER TO postgres;

--
-- Name: institution_instructors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_instructors (
    institution_id uuid NOT NULL,
    instructor_id uuid
);


ALTER TABLE public.institution_instructors OWNER TO postgres;

--
-- Name: institution_manual_instructors; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_manual_instructors (
    institution_id uuid NOT NULL,
    avatar_url character varying(255),
    name character varying(255),
    resume character varying(255)
);


ALTER TABLE public.institution_manual_instructors OWNER TO postgres;

--
-- Name: institution_specialties; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_specialties (
    institution_id uuid NOT NULL,
    specialty character varying(255)
);


ALTER TABLE public.institution_specialties OWNER TO postgres;

--
-- Name: institution_student_orgs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_student_orgs (
    institution_id uuid NOT NULL,
    org_id character varying(255)
);


ALTER TABLE public.institution_student_orgs OWNER TO postgres;

--
-- Name: institution_universities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institution_universities (
    institution_id uuid NOT NULL,
    university_name character varying(255)
);


ALTER TABLE public.institution_universities OWNER TO postgres;

--
-- Name: institutions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.institutions (
    id uuid NOT NULL,
    address character varying(1000),
    admin_note character varying(1000),
    city character varying(255),
    contact_email character varying(255),
    contact_phone character varying(255),
    created_at timestamp(6) with time zone,
    description character varying(2000),
    is_active boolean NOT NULL,
    logo_url character varying(255),
    name character varying(255),
    province character varying(255),
    registration_number character varying(255),
    reviewed_at timestamp(6) with time zone,
    reviewed_by uuid,
    type character varying(255),
    updated_at timestamp(6) with time zone,
    verification_status character varying(255),
    channel_id uuid,
    owner_user_id uuid,
    average_rating double precision NOT NULL,
    dependency_description character varying(2000),
    is_subsidiary boolean DEFAULT false,
    review_count integer NOT NULL,
    CONSTRAINT institutions_type_check CHECK (((type)::text = ANY ((ARRAY['CLUB'::character varying, 'SCIENTIFIC_ASSOCIATION'::character varying, 'INSTITUTE'::character varying, 'STUDENT_ORG'::character varying, 'RESEARCH_CENTER'::character varying, 'INDEPENDENT'::character varying, 'ASSOCIATION'::character varying, 'ACADEMY'::character varying, 'COMMUNITY'::character varying])::text[]))),
    CONSTRAINT institutions_verification_status_check CHECK (((verification_status)::text = ANY ((ARRAY['NONE'::character varying, 'PENDING_VERIFICATION'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.institutions OWNER TO postgres;

--
-- Name: locked_contents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.locked_contents (
    id uuid NOT NULL,
    content_type character varying(255),
    created_at timestamp(6) with time zone,
    description text,
    encryption_key character varying(500),
    lock_status character varying(255),
    price_rials bigint NOT NULL,
    purchase_count integer NOT NULL,
    storage_key character varying(500),
    thumbnail_url character varying(255),
    title character varying(500),
    updated_at timestamp(6) with time zone,
    view_count integer NOT NULL,
    channel_id uuid,
    uploader_id uuid,
    CONSTRAINT locked_contents_lock_status_check CHECK (((lock_status)::text = ANY ((ARRAY['UNLOCKED'::character varying, 'LOCKED'::character varying, 'ARCHIVED'::character varying])::text[])))
);


ALTER TABLE public.locked_contents OWNER TO postgres;

--
-- Name: message_amplitudes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.message_amplitudes (
    message_id uuid NOT NULL,
    amplitudes integer
);


ALTER TABLE public.message_amplitudes OWNER TO postgres;

--
-- Name: message_reactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.message_reactions (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    reaction character varying(255),
    message_id uuid,
    user_id uuid
);


ALTER TABLE public.message_reactions OWNER TO postgres;

--
-- Name: messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.messages (
    id uuid NOT NULL,
    content character varying(10000),
    created_at timestamp(6) with time zone,
    edited_at timestamp(6) with time zone,
    forwarded_from character varying(255),
    is_edited boolean NOT NULL,
    is_pinned boolean NOT NULL,
    media_url character varying(255),
    pinned_at timestamp(6) with time zone,
    pinned_by_id uuid,
    scheduled_at timestamp(6) with time zone,
    status character varying(255),
    type character varying(255),
    chat_id uuid,
    poll_id uuid,
    reply_to_id uuid,
    sender_id uuid,
    action_label character varying(255),
    action_url character varying(255),
    timer_target_at timestamp(6) with time zone,
    CONSTRAINT messages_status_check CHECK (((status)::text = ANY ((ARRAY['SENDING'::character varying, 'SENT'::character varying, 'DELIVERED'::character varying, 'READ'::character varying, 'FAILED'::character varying, 'SCHEDULED'::character varying])::text[]))),
    CONSTRAINT messages_type_check CHECK (((type)::text = ANY (ARRAY['TEXT'::text, 'IMAGE'::text, 'VIDEO'::text, 'VIDEO_NOTE'::text, 'VOICE'::text, 'AUDIO'::text, 'FILE'::text, 'LOCATION'::text, 'CONTACT'::text, 'STICKER'::text, 'GIF'::text, 'POLL'::text, 'LINK'::text])))
);


ALTER TABLE public.messages OWNER TO postgres;

--
-- Name: notifications; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifications (
    id uuid NOT NULL,
    actor_avatar_url character varying(255),
    actor_id uuid,
    actor_name character varying(255),
    body character varying(1000),
    created_at timestamp(6) with time zone,
    is_read boolean NOT NULL,
    is_subscription_notification boolean NOT NULL,
    notification_tier character varying(10) NOT NULL,
    related_entity_id uuid,
    title character varying(255),
    type character varying(255),
    user_id uuid,
    CONSTRAINT notifications_type_check CHECK (((type)::text = ANY ((ARRAY['FOLLOW'::character varying, 'FOLLOW_REQUEST'::character varying, 'COLLABORATION_REQUEST'::character varying, 'COLLABORATION_ACCEPTED'::character varying, 'COLLABORATION_REJECTED'::character varying, 'NEW_MESSAGE'::character varying, 'SYSTEM'::character varying])::text[])))
);


ALTER TABLE public.notifications OWNER TO postgres;

--
-- Name: official_hashtags; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.official_hashtags (
    id uuid NOT NULL,
    branch_channel_id uuid,
    category character varying(255),
    created_at timestamp(6) with time zone,
    display_name_fa character varying(255),
    is_active boolean NOT NULL,
    national_channel_id uuid,
    tag character varying(255),
    university_channel_id uuid
);


ALTER TABLE public.official_hashtags OWNER TO postgres;

--
-- Name: otp_codes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.otp_codes (
    id uuid NOT NULL,
    code character varying(255),
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    is_used boolean NOT NULL,
    phone_number character varying(255)
);


ALTER TABLE public.otp_codes OWNER TO postgres;

--
-- Name: panel_admin_permissions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.panel_admin_permissions (
    admin_id uuid NOT NULL,
    permission character varying(255)
);


ALTER TABLE public.panel_admin_permissions OWNER TO postgres;

--
-- Name: panel_admins; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.panel_admins (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    display_name character varying(255),
    is_super_admin boolean NOT NULL,
    password_hash character varying(255) NOT NULL,
    username character varying(255) NOT NULL
);


ALTER TABLE public.panel_admins OWNER TO postgres;

--
-- Name: poll_options; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.poll_options (
    id uuid NOT NULL,
    text character varying(255),
    vote_count integer NOT NULL,
    poll_id uuid
);


ALTER TABLE public.poll_options OWNER TO postgres;

--
-- Name: poll_votes; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.poll_votes (
    id uuid NOT NULL,
    voted_at timestamp(6) with time zone,
    option_id uuid,
    poll_id uuid,
    user_id uuid
);


ALTER TABLE public.poll_votes OWNER TO postgres;

--
-- Name: polls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.polls (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    is_anonymous boolean NOT NULL,
    is_multiple_choice boolean NOT NULL,
    question character varying(255),
    creator_id uuid
);


ALTER TABLE public.polls OWNER TO postgres;

--
-- Name: promotion_media_urls; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.promotion_media_urls (
    promotion_id uuid NOT NULL,
    media_url character varying(255)
);


ALTER TABLE public.promotion_media_urls OWNER TO postgres;

--
-- Name: refresh_tokens; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.refresh_tokens (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    is_revoked boolean NOT NULL,
    session_id uuid,
    token character varying(255),
    user_id uuid
);


ALTER TABLE public.refresh_tokens OWNER TO postgres;

--
-- Name: riddle_options; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.riddle_options (
    id uuid NOT NULL,
    display_order integer NOT NULL,
    text character varying(255),
    riddle_id uuid
);


ALTER TABLE public.riddle_options OWNER TO postgres;

--
-- Name: role_channel_mappings; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.role_channel_mappings (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    educational_role character varying(50) NOT NULL,
    grade_level character varying(255),
    major character varying(255),
    channel_id uuid NOT NULL
);


ALTER TABLE public.role_channel_mappings OWNER TO postgres;

--
-- Name: smart_folder_rules; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.smart_folder_rules (
    id uuid NOT NULL,
    classification character varying(255),
    display_order integer NOT NULL,
    folder_type character varying(255),
    icon_name character varying(255),
    is_active boolean NOT NULL,
    label_fa character varying(255),
    CONSTRAINT smart_folder_rules_classification_check CHECK (((classification)::text = ANY ((ARRAY['GENERAL'::character varying, 'VERIFIED_TEACHER'::character varying, 'ELM_CLUB_INSTITUTION'::character varying, 'COURSE_CHANNEL'::character varying, 'HASHTAG_NATIONAL'::character varying, 'HASHTAG_UNIVERSITY'::character varying, 'HASHTAG_BRANCH'::character varying])::text[]))),
    CONSTRAINT smart_folder_rules_folder_type_check CHECK (((folder_type)::text = ANY ((ARRAY['TEACHERS'::character varying, 'ELM_CLUB'::character varying, 'COURSES'::character varying, 'PURCHASED'::character varying])::text[])))
);


ALTER TABLE public.smart_folder_rules OWNER TO postgres;

--
-- Name: startup_ideas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.startup_ideas (
    id uuid NOT NULL,
    contact_info character varying(255),
    created_at timestamp(6) with time zone,
    description text,
    title character varying(255) NOT NULL,
    user_id uuid
);


ALTER TABLE public.startup_ideas OWNER TO postgres;

--
-- Name: stories; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.stories (
    id uuid NOT NULL,
    caption character varying(1000),
    created_at timestamp(6) with time zone,
    duration_seconds integer NOT NULL,
    expires_at timestamp(6) with time zone,
    media_url character varying(255),
    type character varying(255),
    channel_id uuid,
    group_id uuid,
    user_id uuid,
    CONSTRAINT stories_type_check CHECK (((type)::text = ANY ((ARRAY['IMAGE'::character varying, 'VIDEO'::character varying])::text[])))
);


ALTER TABLE public.stories OWNER TO postgres;

--
-- Name: story_replies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.story_replies (
    id uuid NOT NULL,
    content character varying(2000),
    created_at timestamp(6) with time zone,
    story_id uuid,
    user_id uuid
);


ALTER TABLE public.story_replies OWNER TO postgres;

--
-- Name: story_views; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.story_views (
    id uuid NOT NULL,
    viewed_at timestamp(6) with time zone,
    story_id uuid,
    user_id uuid
);


ALTER TABLE public.story_views OWNER TO postgres;

--
-- Name: student_orgs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.student_orgs (
    id uuid NOT NULL,
    display_order integer NOT NULL,
    name character varying(255)
);


ALTER TABLE public.student_orgs OWNER TO postgres;

--
-- Name: subscription_plans; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.subscription_plans (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    duration_days integer NOT NULL,
    features text,
    is_active boolean NOT NULL,
    max_promotions integer NOT NULL,
    name character varying(255),
    price_rials bigint NOT NULL,
    tier character varying(255),
    CONSTRAINT subscription_plans_tier_check CHECK (((tier)::text = ANY ((ARRAY['NONE'::character varying, 'BASIC'::character varying, 'PREMIUM'::character varying, 'INSTITUTIONAL'::character varying])::text[])))
);


ALTER TABLE public.subscription_plans OWNER TO postgres;

--
-- Name: teacher_verif_documents; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.teacher_verif_documents (
    request_id uuid NOT NULL,
    document_url character varying(255)
);


ALTER TABLE public.teacher_verif_documents OWNER TO postgres;

--
-- Name: teacher_verification_requests; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.teacher_verification_requests (
    id uuid NOT NULL,
    admin_note character varying(1000),
    created_at timestamp(6) with time zone,
    full_name character varying(255),
    institution character varying(255),
    national_code character varying(10),
    reviewed_at timestamp(6) with time zone,
    reviewed_by uuid,
    status character varying(255),
    teaching_field character varying(255),
    updated_at timestamp(6) with time zone,
    user_id uuid,
    CONSTRAINT teacher_verification_requests_status_check CHECK (((status)::text = ANY ((ARRAY['NONE'::character varying, 'PENDING_VERIFICATION'::character varying, 'APPROVED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.teacher_verification_requests OWNER TO postgres;

--
-- Name: universities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.universities (
    id uuid NOT NULL,
    article_count integer NOT NULL,
    city character varying(255),
    country character varying(255),
    created_at timestamp(6) with time zone,
    departments character varying(5000),
    established_year integer,
    facilities character varying(2000),
    faculties character varying(2000),
    honors character varying(5000),
    image_url character varying(255),
    iran_rank integer NOT NULL,
    journal_count integer NOT NULL,
    last_admission_capacity character varying(5000),
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    ministry_name character varying(255),
    name character varying(255) NOT NULL,
    paper_count integer NOT NULL,
    professor_count integer NOT NULL,
    professor_names character varying(5000),
    province character varying(255),
    publication_count integer NOT NULL,
    student_count integer NOT NULL,
    student_orgs character varying(5000),
    type character varying(255),
    website_url character varying(255),
    world_rank integer NOT NULL,
    rankings text
);


ALTER TABLE public.universities OWNER TO postgres;

--
-- Name: user_favorite_courses; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_favorite_courses (
    user_id uuid NOT NULL,
    course_id uuid
);


ALTER TABLE public.user_favorite_courses OWNER TO postgres;

--
-- Name: user_follows; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_follows (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    status character varying(255),
    follower_id uuid,
    following_id uuid,
    CONSTRAINT user_follows_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.user_follows OWNER TO postgres;

--
-- Name: user_profile_details; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_profile_details (
    id uuid NOT NULL,
    academy_hashtags character varying(1000),
    academy_name character varying(255),
    achievements character varying(2000),
    city character varying(255),
    education character varying(255),
    faculty character varying(255),
    field_of_study character varying(255),
    interests character varying(2000),
    is_teacher boolean NOT NULL,
    province character varying(255),
    skills character varying(2000),
    teaching_field character varying(255),
    teaching_university character varying(255),
    university character varying(255),
    updated_at timestamp(6) with time zone,
    work_experience character varying(5000),
    user_id uuid,
    is_graduated boolean NOT NULL
);


ALTER TABLE public.user_profile_details OWNER TO postgres;

--
-- Name: user_profile_fields; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_profile_fields (
    profile_id uuid NOT NULL,
    fields_of_study character varying(255)
);


ALTER TABLE public.user_profile_fields OWNER TO postgres;

--
-- Name: user_profile_universities; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_profile_universities (
    profile_id uuid NOT NULL,
    universities character varying(255)
);


ALTER TABLE public.user_profile_universities OWNER TO postgres;

--
-- Name: user_sessions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_sessions (
    id uuid NOT NULL,
    app_version character varying(255),
    created_at timestamp(6) with time zone,
    device_name character varying(255),
    is_active boolean NOT NULL,
    last_active_at timestamp(6) with time zone,
    last_active_ip character varying(255),
    os_version character varying(255),
    platform character varying(255),
    user_id uuid NOT NULL
);


ALTER TABLE public.user_sessions OWNER TO postgres;

--
-- Name: user_subscriptions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_subscriptions (
    id uuid NOT NULL,
    auto_renew boolean NOT NULL,
    created_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    is_active boolean NOT NULL,
    starts_at timestamp(6) with time zone,
    transaction_id uuid,
    plan_id uuid,
    user_id uuid
);


ALTER TABLE public.user_subscriptions OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id uuid NOT NULL,
    avatar_url character varying(255),
    bio character varying(500),
    bio_channel_id1 uuid,
    bio_channel_id2 uuid,
    birth_date date,
    created_at timestamp(6) with time zone,
    display_name character varying(255),
    educational_role character varying(50),
    faculty character varying(255),
    first_name character varying(255),
    grade_level character varying(255),
    institution_id uuid,
    institution_logo_url character varying(255),
    institution_name character varying(255),
    is_online boolean NOT NULL,
    is_premium boolean NOT NULL,
    last_name character varying(255),
    last_seen timestamp(6) with time zone,
    major character varying(255),
    national_code character varying(255),
    online_visibility character varying(255),
    password_hash character varying(255),
    phone_number character varying(255),
    phone_visibility character varying(255),
    points bigint NOT NULL,
    profile_visibility character varying(255),
    role character varying(20) NOT NULL,
    username character varying(255),
    average_rating double precision NOT NULL,
    official_channel_id uuid,
    review_count integer NOT NULL,
    CONSTRAINT users_online_visibility_check CHECK (((online_visibility)::text = ANY ((ARRAY['EVERYONE'::character varying, 'CONTACTS'::character varying, 'NOBODY'::character varying])::text[]))),
    CONSTRAINT users_phone_visibility_check CHECK (((phone_visibility)::text = ANY ((ARRAY['EVERYONE'::character varying, 'CONTACTS'::character varying, 'NOBODY'::character varying])::text[]))),
    CONSTRAINT users_profile_visibility_check CHECK (((profile_visibility)::text = ANY ((ARRAY['EVERYONE'::character varying, 'CONTACTS'::character varying, 'NOBODY'::character varying])::text[]))),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['NORMAL'::character varying, 'TEACHER'::character varying, 'INSTITUTION'::character varying, 'ADMIN'::character varying, 'SUPER_ADMIN'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: wallet_transactions; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.wallet_transactions (
    id uuid NOT NULL,
    amount bigint NOT NULL,
    balance_after bigint NOT NULL,
    created_at timestamp(6) with time zone,
    description character varying(500),
    gateway_ref character varying(255),
    reference_id uuid,
    reference_type character varying(255),
    type character varying(255),
    wallet_id uuid,
    CONSTRAINT wallet_transactions_type_check CHECK (((type)::text = ANY ((ARRAY['DEPOSIT'::character varying, 'WITHDRAWAL'::character varying, 'PURCHASE'::character varying, 'REFUND'::character varying, 'SUBSCRIPTION'::character varying, 'INTERNAL_TEST_PURCHASE'::character varying])::text[])))
);


ALTER TABLE public.wallet_transactions OWNER TO postgres;

--
-- Name: wallets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.wallets (
    id uuid NOT NULL,
    balance bigint NOT NULL,
    created_at timestamp(6) with time zone,
    is_active boolean NOT NULL,
    updated_at timestamp(6) with time zone,
    user_id uuid
);


ALTER TABLE public.wallets OWNER TO postgres;

--
-- Data for Name: ad_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ad_requests (id, created_at, message_content, message_media_url, message_type, reviewed_at, reviewed_by, source_id, source_message_id, source_type, status, requester_id, target_channel_id) FROM stdin;
\.


--
-- Data for Name: ai_bot_messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ai_bot_messages (id, bot_id, content, created_at, role, user_id, action_label, action_url) FROM stdin;
\.


--
-- Data for Name: ai_bots; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.ai_bots (id, avatar_url, bot_type, category, created_at, description, display_order, is_active, name) FROM stdin;
d5c38d24-ade2-4cad-b674-cb476158b14f	\N	chatgpt	GENERAL	2026-05-08 06:54:05.623677+00	مدل هوش مصنوعی OpenAI	1	t	ChatGPT
992eeef1-4d10-46b7-afa9-5c57816e8151	\N	gemini	GENERAL	2026-05-08 06:54:05.623686+00	مدل هوش مصنوعی Google	2	t	Gemini
8a8b4ccd-0596-42cd-b4ad-34e970d56eb8	\N	deepseek	GENERAL	2026-05-08 06:54:05.623692+00	مدل هوش مصنوعی DeepSeek	3	t	DeepSeek
6945f8aa-bca9-4ddb-b120-f9ebdecbd109	\N	grok	GENERAL	2026-05-08 06:54:05.623696+00	مدل هوش مصنوعی xAI	4	t	Grok
6efcc0a0-16f9-456c-8ed7-0a156b1eabff	\N	copilot	GENERAL	2026-05-08 06:54:05.623701+00	مدل هوش مصنوعی Microsoft	5	t	Copilot
bc392727-0e7e-4074-8dba-10313b72b305	\N	exam_assistant	SPECIALIST	2026-05-08 06:54:05.623707+00	کمک در آمادگی برای امتحانات	10	t	دستیار تخصصی امتحان
ab0f8191-caee-4bc0-9f8d-67dfb0a9c096	\N	translation_assistant	SPECIALIST	2026-05-08 06:54:05.623712+00	ترجمه متون تخصصی و عمومی	11	t	دستیار تخصصی ترجمه
c3ec70fe-5159-408c-95da-dde35a8b4719	\N	article_assistant	SPECIALIST	2026-05-08 06:54:05.623717+00	کمک در نگارش مقالات علمی	12	t	دستیار تخصصی نگارش مقاله
10832e99-0493-4cae-8502-d7244dbdaa1f	\N	file_analysis	SPECIALIST	2026-05-08 06:54:05.623722+00	تحلیل و بررسی فایل‌ها ⭐	13	t	دستیار تخصصی تحلیل فایل
a5d2544f-44c6-4c99-a847-4e555d467898	\N	image_generation	SPECIALIST	2026-05-08 06:54:05.623729+00	تولید تصاویر با هوش مصنوعی ⭐	14	t	دستیار تخصصی تولید تصویر
a388c1e6-82c6-4fdc-9ebf-11d9d066ce60	\N	powerpoint_assistant	SPECIALIST	2026-05-08 06:54:05.623733+00	ساخت خودکار پاورپوینت ⭐	15	t	دستیار تخصصی ساخت پاورپوینت
477bf515-6e50-482f-a542-353cbb11d4ed	\N	clip_assistant	SPECIALIST	2026-05-08 06:54:05.623739+00	ساخت کلیپ‌های علمی ⭐	16	t	دستیار تخصصی ساخت کلیپ علمی
0b729574-e5aa-48b0-947f-208715c8a73a	\N	paper_search	SPECIALIST	2026-05-08 06:54:05.623743+00	جستجو در سایت‌های ایرانی + Sci-Hub ⭐	17	t	جستجوی مقالات
6e027d7b-49cd-4dc3-8195-6f5bb857743e	https://img.icons8.com/fluency/96/graduation-cap.png	mosbat_elm	SPECIALIST	2026-05-12 13:17:54.74159+00	دستیار هوشمند شما در دوره‌های آموزشی مثبت علم 🎓	0	t	ربات مثبت علم
\.


--
-- Data for Name: channel_post_amplitudes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.channel_post_amplitudes (channel_post_id, amplitudes) FROM stdin;
\.


--
-- Data for Name: channel_post_comments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.channel_post_comments (id, content, created_at, post_id, user_id) FROM stdin;
\.


--
-- Data for Name: channel_post_reactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.channel_post_reactions (id, created_at, reaction, post_id, user_id) FROM stdin;
\.


--
-- Data for Name: channel_posts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.channel_posts (id, ad_label, ad_source_channel_id, comments_enabled, content, created_at, edited_at, forwarded_from, is_ad, is_pinned, media_url, pinned_at, pinned_by_id, scheduled_at, type, view_count, channel_id, poll_id, action_label, action_url, timer_target_at) FROM stdin;
\.


--
-- Data for Name: channel_subscribers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.channel_subscribers (id, can_add_members, can_edit_info, can_post_story, can_remove_members, is_admin, is_archived, is_mandatory, is_muted, is_pinned, subscribed_at, channel_id, user_id) FROM stdin;
379c7ae5-40f9-400f-ac5a-6b0442a12fcf	f	f	f	f	t	f	f	f	f	2026-05-15 22:01:43.554962+00	bc7220de-d0b2-4d44-a1ce-f7fa05829f57	e7a11dde-6c43-480b-b0d7-daadd8648e90
50464cda-96ad-45fc-a59f-f1c00add0394	f	f	f	f	t	f	f	f	f	2026-05-15 22:06:48.955306+00	4f77249f-2bc6-40d2-80d9-9bf5adfd8afa	e7a11dde-6c43-480b-b0d7-daadd8648e90
26fda53f-feca-475c-963e-6d5e149a0907	f	f	f	f	t	f	f	f	f	2026-05-15 22:09:18.821559+00	a2e21484-de8b-4bff-9ee4-d7fee181ac33	e7a11dde-6c43-480b-b0d7-daadd8648e90
f0aaae51-16b7-4bb7-81ee-1ce66c89683e	f	f	f	f	t	f	f	f	f	2026-05-15 22:09:31.395509+00	761cb9b8-366d-4e49-9917-cfd4db249f00	e7a11dde-6c43-480b-b0d7-daadd8648e90
09a8d665-7e10-4e8e-9c43-1d601352385a	f	f	f	f	t	f	f	f	f	2026-05-15 22:09:43.074127+00	6ce57cb8-e1cc-4c68-becd-ce48a9781657	e7a11dde-6c43-480b-b0d7-daadd8648e90
335ed4cb-4722-40a2-bb5c-026a9d513b94	f	f	f	f	t	f	f	f	f	2026-05-15 22:10:15.494899+00	b263595c-96e2-4aca-8e68-0ddd77b12f02	e7a11dde-6c43-480b-b0d7-daadd8648e90
fba9fd7e-e6af-4b46-b0ca-8a827d196b8d	f	f	f	f	t	f	f	f	f	2026-05-15 22:10:33.484693+00	f3f89a5e-d73c-4bc1-89a2-3ae9d98877cb	e7a11dde-6c43-480b-b0d7-daadd8648e90
5d268eb6-99fd-44f7-90c8-e2fc94d3c59f	f	f	f	f	t	f	f	f	f	2026-05-15 22:11:49.788189+00	bed57b28-3383-458a-8e18-50affe71d7ba	e7a11dde-6c43-480b-b0d7-daadd8648e90
cfa77c54-f02c-46ee-8138-12f8c4b48bd7	f	f	f	f	t	f	f	f	f	2026-05-15 22:13:07.968081+00	c3354bd7-9959-4696-89ca-d043924d1507	e7a11dde-6c43-480b-b0d7-daadd8648e90
b0020570-f2c0-412d-a38e-e0eb5c12c582	f	f	f	f	t	f	f	f	f	2026-05-15 22:13:40.256881+00	483ddd7a-afeb-4f2d-bc1d-43bb30ed9796	e7a11dde-6c43-480b-b0d7-daadd8648e90
33c62ffb-ba63-4e53-8a7b-4db79ba5e9cd	f	f	f	f	t	f	f	f	f	2026-05-15 22:06:03.850118+00	3e7d4727-e428-4c76-91c2-f18fb4b9464e	e7a11dde-6c43-480b-b0d7-daadd8648e90
9d9e8cc9-930c-4e87-8a3b-3b1a182b6470	f	f	f	f	t	f	f	f	f	2026-05-15 22:25:45.840505+00	fc3faea1-de36-4f32-b51b-96b7a0cebbc7	e7a11dde-6c43-480b-b0d7-daadd8648e90
6078088c-67d1-4a59-923b-c09c70cc251c	f	f	f	f	t	f	f	f	f	2026-05-15 22:27:37.760642+00	0888de16-b57d-42cf-9e03-2a1978424b4a	e7a11dde-6c43-480b-b0d7-daadd8648e90
8f562eaf-9eff-4df6-90a3-808fdda91427	f	f	f	f	t	f	f	f	f	2026-05-15 22:28:01.823966+00	ba0c8ceb-ad96-435f-99bc-389a6952ceb4	e7a11dde-6c43-480b-b0d7-daadd8648e90
437febb6-d56c-4305-a708-d6a9a2cd877b	f	f	f	f	t	f	f	f	f	2026-05-15 22:28:35.293557+00	fac94efc-2b31-4faa-8448-2bc79e5863f7	e7a11dde-6c43-480b-b0d7-daadd8648e90
a7d307cb-8989-421a-880c-e544ae6547e3	f	f	f	f	t	f	f	f	f	2026-05-15 22:50:29.647527+00	e7c6f928-621c-41a5-8adc-78bbb5dc2aa0	e7a11dde-6c43-480b-b0d7-daadd8648e90
a69efda1-1d43-4451-a531-91870ab370fc	f	f	f	f	t	f	f	f	f	2026-05-15 22:51:01.414756+00	881f7848-f688-4fff-a641-7300bde115aa	e7a11dde-6c43-480b-b0d7-daadd8648e90
14eacc71-cd45-4e0a-9f20-60c297285eb7	f	f	f	f	t	f	f	f	f	2026-05-15 22:51:38.440918+00	e02acb28-b80e-4329-a89e-ab1e2f75ba93	e7a11dde-6c43-480b-b0d7-daadd8648e90
311505ca-7caa-4891-ab20-90f0480dc2a7	f	f	f	f	t	f	f	f	f	2026-05-15 22:52:11.988949+00	f6a63094-c194-403e-9886-cf7657581ba8	e7a11dde-6c43-480b-b0d7-daadd8648e90
15b8378c-c9a8-40a6-a047-1fbaf8bbece8	f	f	f	f	t	f	f	f	f	2026-05-15 22:52:35.333834+00	8307d5f7-f943-4068-9f20-f4cd4a040b07	e7a11dde-6c43-480b-b0d7-daadd8648e90
8e9c4a35-7241-48c3-9c60-8d48c3c204d7	f	f	f	f	t	f	f	f	f	2026-05-15 22:57:04.678851+00	62d920c3-791a-441c-9772-27b3096f4d5b	e7a11dde-6c43-480b-b0d7-daadd8648e90
bef70f64-48fd-4ffc-9aef-c50ea9b2530b	f	f	f	f	t	f	f	f	f	2026-05-15 22:59:13.473899+00	4d5a37e4-add8-477d-993e-f3f2415dba97	e7a11dde-6c43-480b-b0d7-daadd8648e90
a3728dc6-708e-4c48-ac47-7d19c3f7acc1	f	f	f	f	t	f	f	f	f	2026-05-15 23:01:50.297294+00	c878da42-55aa-4189-b01b-2be936325adf	e7a11dde-6c43-480b-b0d7-daadd8648e90
b3bf6209-bdbd-4dcb-8d67-6913865f3cf3	f	f	f	f	t	f	f	f	f	2026-05-15 23:04:24.258249+00	95014c42-8df2-424c-8980-1f118587f8fd	e7a11dde-6c43-480b-b0d7-daadd8648e90
3fad7bae-fa44-44f4-b173-619d840c91ed	f	f	f	f	t	f	f	f	f	2026-05-15 23:17:20.407949+00	322d2cb1-ac53-428f-8359-c53d61a0260f	e7a11dde-6c43-480b-b0d7-daadd8648e90
7a339d6e-e710-4e58-b29f-a5d5c12cc2e0	f	f	f	f	t	f	f	f	f	2026-05-15 23:17:54.162557+00	dc89852c-01ee-43a9-9cc5-1caba7c6bd44	e7a11dde-6c43-480b-b0d7-daadd8648e90
a0c05a3e-cc49-4b6b-9c1a-c7f652878df2	f	f	f	f	t	f	f	f	f	2026-05-15 23:18:20.091707+00	e856143c-6c21-4c0b-bee8-34cca6b39c2a	e7a11dde-6c43-480b-b0d7-daadd8648e90
aff481cc-fcb4-45f8-8198-d68dfc2e9cd3	f	f	f	f	t	f	f	f	f	2026-05-15 23:18:42.48087+00	239c7798-2380-4f88-ac8c-4f12f0b63d5f	e7a11dde-6c43-480b-b0d7-daadd8648e90
ad2a2496-208d-449a-87ab-f7aed92f9802	f	f	f	f	t	f	f	f	f	2026-05-15 23:19:04.781053+00	20bc2dbe-3143-4759-b9b2-03b77f9e0d06	e7a11dde-6c43-480b-b0d7-daadd8648e90
b309b907-3428-40a3-9aa3-9cb2e10a2823	f	f	f	f	t	f	f	f	f	2026-05-15 23:19:26.775194+00	ffccaa49-ecfa-4db2-aae3-a29d79fd86c9	e7a11dde-6c43-480b-b0d7-daadd8648e90
3e4aebfa-3cf0-4f82-bdf6-a0e9a6c98f93	f	f	f	f	t	f	f	f	f	2026-05-15 23:19:44.404974+00	e40ac252-99da-47fe-9ad7-2c72f0e77cce	e7a11dde-6c43-480b-b0d7-daadd8648e90
8f82fc2f-dca5-4d95-a2d5-5184c06a7377	f	f	f	f	t	f	f	f	f	2026-05-15 23:20:07.424153+00	57665433-379e-418a-b256-7e4783258d0c	e7a11dde-6c43-480b-b0d7-daadd8648e90
7570478c-bbf2-473d-bac8-19f548430cff	f	f	f	f	t	f	f	f	f	2026-05-15 23:20:27.345385+00	f3b17bb1-f417-4f70-ba58-bd3dcd0a3811	e7a11dde-6c43-480b-b0d7-daadd8648e90
db1970cd-3347-48c0-9f75-db2bbea709fc	f	f	f	f	t	f	f	f	f	2026-05-15 23:20:45.8277+00	1ccbdfa0-3c1d-4db8-8066-1014186890f3	e7a11dde-6c43-480b-b0d7-daadd8648e90
6711c274-ccfe-431e-87fe-5e3e19dd9284	f	f	f	f	t	f	f	f	f	2026-05-15 23:21:02.714512+00	e3ea38ad-47d1-4848-b55b-f703e8e52d73	e7a11dde-6c43-480b-b0d7-daadd8648e90
bef77990-8445-427a-a7d2-33a9647e6cdf	f	f	f	f	t	f	f	f	f	2026-05-15 23:21:22.434524+00	f7cdb9fe-9d1f-4002-811d-1c7548fde79c	e7a11dde-6c43-480b-b0d7-daadd8648e90
ff515f2a-4775-4b54-a9cb-261e0bdd99a8	f	f	f	f	t	f	f	f	f	2026-05-15 23:21:40.393309+00	8ada6b31-9cce-4d40-a7ba-620a085e0d0c	e7a11dde-6c43-480b-b0d7-daadd8648e90
1a2c1527-4a5b-4832-aee0-6a68d6fc8e8a	f	f	f	f	t	f	f	f	f	2026-05-15 23:22:00.741109+00	47c659ab-4c14-4e2c-a909-3e69852c848f	e7a11dde-6c43-480b-b0d7-daadd8648e90
433b0903-4abc-4d1a-b69f-fd0152e6c92f	f	f	f	f	t	f	f	f	f	2026-05-15 23:22:18.884286+00	fcefeeb3-d7b9-4c7b-af98-3fa8dbbca2e5	e7a11dde-6c43-480b-b0d7-daadd8648e90
4fdf795e-da8f-4f3c-95cc-0feba231abcf	f	f	f	f	t	f	f	f	f	2026-05-15 23:22:39.58737+00	66565333-68c3-4bb5-8998-ca52458d01de	e7a11dde-6c43-480b-b0d7-daadd8648e90
407bc03a-b770-4279-9af1-72a842a54cb9	f	f	f	f	t	f	f	f	f	2026-05-15 23:23:05.259126+00	6280cdfc-e0d6-4787-8ab9-fda7ad6b054f	e7a11dde-6c43-480b-b0d7-daadd8648e90
91f1c345-6272-4107-b56a-cbb1b6d836f7	f	f	f	f	t	f	f	f	f	2026-05-15 23:23:28.212003+00	274ed77d-b463-4975-88e5-f39e14abe324	e7a11dde-6c43-480b-b0d7-daadd8648e90
9a0f7a85-7bbe-4483-a470-4665a8753036	f	f	f	f	t	f	f	f	f	2026-05-15 23:26:31.674646+00	1451ae0f-037c-4b3a-a109-aaa627c68275	e7a11dde-6c43-480b-b0d7-daadd8648e90
8b430de5-597b-46c1-bebf-a4db0f81eafd	f	f	f	f	t	f	f	f	f	2026-05-15 23:26:52.858186+00	97a68ad4-24cc-4139-8bc3-97cba88c6ec3	e7a11dde-6c43-480b-b0d7-daadd8648e90
c9c5e42f-1074-4cee-8a1b-49d4d6196fee	f	f	f	f	t	f	f	f	f	2026-05-15 23:27:10.121695+00	bfbc9004-984b-4840-8f13-2c86f34b272d	e7a11dde-6c43-480b-b0d7-daadd8648e90
8cbbd4dc-3558-4370-a741-94c5e7c6b1c6	f	f	f	f	t	f	f	f	f	2026-05-15 23:27:27.868731+00	b14bebdf-5f50-4455-b5fe-15c9c8d262ad	e7a11dde-6c43-480b-b0d7-daadd8648e90
fb648447-4284-4e67-be65-e08590f1e4de	f	f	f	f	t	f	f	f	f	2026-05-15 23:27:54.408387+00	5786d035-132f-4acb-8076-1e67600e1878	e7a11dde-6c43-480b-b0d7-daadd8648e90
462fbfdc-4de9-4c6a-b696-c639777069eb	f	f	f	f	t	f	f	f	f	2026-05-15 23:28:10.000791+00	86de4004-bf09-4330-b9c2-bce2d8e439d5	e7a11dde-6c43-480b-b0d7-daadd8648e90
001a7561-1a0b-4891-9a72-7c2025c19618	f	f	f	f	t	f	f	f	f	2026-05-15 23:28:30.009392+00	0ce6f2bb-1e17-4197-b6a1-ea557bd9ae36	e7a11dde-6c43-480b-b0d7-daadd8648e90
85f5b090-2dd4-4ce2-a563-8ddeaff84fe3	f	f	f	f	t	f	f	f	f	2026-05-15 23:28:49.607405+00	d5a1f9d5-34ca-45d8-a62c-b616168a76c5	e7a11dde-6c43-480b-b0d7-daadd8648e90
0aadf241-3c4c-436c-96df-b28b5cad8c07	f	f	f	f	t	f	f	f	f	2026-05-15 23:29:08.476262+00	070b883b-8c6b-4ddc-8b8a-d36631ac4527	e7a11dde-6c43-480b-b0d7-daadd8648e90
51b50147-1b3c-4ad6-a442-2b27ec60e9f4	f	f	f	f	t	f	f	f	f	2026-05-15 23:29:28.456745+00	106dccdf-2ba6-41ab-9a94-955023b2a778	e7a11dde-6c43-480b-b0d7-daadd8648e90
ca4a2be7-2037-42b7-a672-4b0e16a9dafd	f	f	f	f	t	f	f	f	f	2026-05-15 23:29:46.679958+00	85e7e57d-0302-4ffc-af7d-023c6665c958	e7a11dde-6c43-480b-b0d7-daadd8648e90
fc39317b-afa8-4ab7-b5c7-af9200f2fdf7	f	f	f	f	t	f	f	f	f	2026-05-15 23:30:07.514141+00	4ba13052-0edc-4a8f-85dd-ff584811e3f9	e7a11dde-6c43-480b-b0d7-daadd8648e90
42128f5d-4cef-47b8-a86c-15bc43e9d08d	f	f	f	f	t	f	f	f	f	2026-05-15 23:30:29.244425+00	a9359003-daf4-42e9-88ae-22f0d429311c	e7a11dde-6c43-480b-b0d7-daadd8648e90
66fed4c4-dfc0-493d-beb5-b899f1f97de1	f	f	f	f	t	f	f	f	f	2026-05-15 23:54:20.092545+00	4d905533-e6de-4d05-9a6c-77c80acb9cca	e7a11dde-6c43-480b-b0d7-daadd8648e90
3900743b-716d-479c-84c5-479418359a62	f	f	f	f	t	f	f	f	f	2026-05-15 23:55:57.691291+00	73d3479f-3d45-4283-a2e3-2b76971c30e7	e7a11dde-6c43-480b-b0d7-daadd8648e90
\.


--
-- Data for Name: channels; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.channels (id, avatar_url, classification, created_at, description, display_mode, institution_id, invite_link, is_official, is_public, is_verified_teacher, name, official_category, public_id, target_city, target_education_level, target_field_of_study, target_province, target_university, owner_id, target_ministry, target_audience_type) FROM stdin;
bc7220de-d0b2-4d44-a1ce-f7fa05829f57	\N	GENERAL	2026-05-15 22:01:43.471751+00		SPECIAL	\N	\N	t	t	f	کانال رسمی اپلیکیشن کلاسور	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
3e7d4727-e428-4c76-91c2-f18fb4b9464e	\N	GENERAL	2026-05-15 22:06:03.816461+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان ایران زمین 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
4f77249f-2bc6-40d2-80d9-9bf5adfd8afa	\N	GENERAL	2026-05-15 22:06:48.93677+00		SPECIAL	\N	\N	t	t	f	کانال رسمی پادکست دانشجویی	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
a2e21484-de8b-4bff-9ee4-d7fee181ac33	\N	GENERAL	2026-05-15 22:09:18.792804+00		SPECIAL	\N	\N	t	t	f	کانال رسمی فریلنسری دانشجویی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
761cb9b8-366d-4e49-9917-cfd4db249f00	\N	GENERAL	2026-05-15 22:09:31.371406+00		SPECIAL	\N	\N	t	t	f	کانال رسمی نشریه دانشجویی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
6ce57cb8-e1cc-4c68-becd-ce48a9781657	\N	GENERAL	2026-05-15 22:09:43.035557+00		SPECIAL	\N	\N	t	t	f	کانال گیف و استیکر	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
b263595c-96e2-4aca-8e68-0ddd77b12f02	\N	GENERAL	2026-05-15 22:10:15.471377+00		SPECIAL	\N	\N	t	t	f	کانال مسابقات ، جشنواره‌ها و کنگره‌های علمی	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
f3f89a5e-d73c-4bc1-89a2-3ae9d98877cb	\N	GENERAL	2026-05-15 22:10:33.465456+00		SPECIAL	\N	\N	t	t	f	کانال سمینارهای آزاد دانشجویی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
bed57b28-3383-458a-8e18-50affe71d7ba	\N	GENERAL	2026-05-15 22:11:49.768684+00		SPECIAL	\N	\N	t	t	f	کانال بزرگ آنلاین شاپ دانشجویی	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
c3354bd7-9959-4696-89ca-d043924d1507	\N	GENERAL	2026-05-15 22:13:07.943442+00		SPECIAL	\N	\N	t	t	f	کانال جزوه و نمونه سوال	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
483ddd7a-afeb-4f2d-bc1d-43bb30ed9796	\N	GENERAL	2026-05-15 22:13:40.189897+00		SPECIAL	\N	\N	t	t	f	کانال کتاب و کتابخوانی ، مسابقات 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
fc3faea1-de36-4f32-b51b-96b7a0cebbc7	\N	GENERAL	2026-05-15 22:25:45.811251+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانش آموزان ریاضی	STUDENTS_IRAN	\N	\N	متوسطه دوم (نظری)	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
0888de16-b57d-42cf-9e03-2a1978424b4a	\N	GENERAL	2026-05-15 22:27:37.383464+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانش آموزان تجربی	STUDENTS_IRAN	\N	\N	متوسطه دوم (نظری)	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
ba0c8ceb-ad96-435f-99bc-389a6952ceb4	\N	GENERAL	2026-05-15 22:28:01.804351+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانش آموزان انسانی	STUDENTS_IRAN	\N	\N	متوسطه دوم (نظری)	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
fac94efc-2b31-4faa-8448-2bc79e5863f7	\N	GENERAL	2026-05-15 22:28:35.279647+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانش آموزان هنر	STUDENTS_IRAN	\N	\N	هنرستان	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e7c6f928-621c-41a5-8adc-78bbb5dc2aa0	\N	GENERAL	2026-05-15 22:50:29.627394+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان وزارت علوم 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	وزارت علوم	\N
881f7848-f688-4fff-a641-7300bde115aa	\N	GENERAL	2026-05-15 22:51:01.394045+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان وزارت بهداشت 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	وزارت بهداشت	\N
e02acb28-b80e-4329-a89e-ab1e2f75ba93	\N	GENERAL	2026-05-15 22:51:38.426163+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه پیام نور 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	پیام نور	\N
f6a63094-c194-403e-9886-cf7657581ba8	\N	GENERAL	2026-05-15 22:52:11.971682+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه فنی و حرفه‌ای 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	فنی حرفه ای	\N
8307d5f7-f943-4068-9f20-f4cd4a040b07	\N	GENERAL	2026-05-15 22:52:35.315007+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه آزاد 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	دانشگاه آزاد	\N
62d920c3-791a-441c-9772-27b3096f4d5b	\N	GENERAL	2026-05-15 22:57:04.65541+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه فرهنگیان 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	فرهنگیان	\N
4d5a37e4-add8-477d-993e-f3f2415dba97	\N	GENERAL	2026-05-15 22:59:13.445673+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه منابع طبیعی	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	منابع طبیعی	\N
c878da42-55aa-4189-b01b-2be936325adf	\N	GENERAL	2026-05-15 23:01:50.265367+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه علمی کاربردی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	علمی کاربردی	\N
95014c42-8df2-424c-8980-1f118587f8fd	\N	GENERAL	2026-05-15 23:04:24.236496+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان هنر	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	هنر	\N
322d2cb1-ac53-428f-8359-c53d61a0260f	\N	GENERAL	2026-05-15 23:17:20.362401+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه‌های استان تهران	STUDENTS_IRAN	\N	\N	\N	\N	تهران	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
dc89852c-01ee-43a9-9cc5-1caba7c6bd44	\N	GENERAL	2026-05-15 23:17:54.129333+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان آذربایجان شرقی 	STUDENTS_IRAN	\N	\N	\N	\N	آذربایجان شرقی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e856143c-6c21-4c0b-bee8-34cca6b39c2a	\N	GENERAL	2026-05-15 23:18:20.072602+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان آذربایجان غربی 	STUDENTS_IRAN	\N	\N	\N	\N	آذربایجان غربی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
239c7798-2380-4f88-ac8c-4f12f0b63d5f	\N	GENERAL	2026-05-15 23:18:42.463496+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان اردبیل	STUDENTS_IRAN	\N	\N	\N	\N	اردبیل	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
20bc2dbe-3143-4759-b9b2-03b77f9e0d06	\N	GENERAL	2026-05-15 23:19:04.756438+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان اصفهان 	STUDENTS_IRAN	\N	\N	\N	\N	اصفهان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
ffccaa49-ecfa-4db2-aae3-a29d79fd86c9	\N	GENERAL	2026-05-15 23:19:26.748048+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان البرز	STUDENTS_IRAN	\N	\N	\N	\N	البرز	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e40ac252-99da-47fe-9ad7-2c72f0e77cce	\N	GENERAL	2026-05-15 23:19:44.380541+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان ایلام	STUDENTS_IRAN	\N	\N	\N	\N	ایلام	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
57665433-379e-418a-b256-7e4783258d0c	\N	GENERAL	2026-05-15 23:20:07.403719+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان بوشهر	STUDENTS_IRAN	\N	\N	\N	\N	بوشهر	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
f3b17bb1-f417-4f70-ba58-bd3dcd0a3811	\N	GENERAL	2026-05-15 23:20:27.322378+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان خراسان جنوبی 	STUDENTS_IRAN	\N	\N	\N	\N	خراسان جنوبی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
1ccbdfa0-3c1d-4db8-8066-1014186890f3	\N	GENERAL	2026-05-15 23:20:45.769498+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان خراسان رضوی 	STUDENTS_IRAN	\N	\N	\N	\N	خراسان رضوی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e3ea38ad-47d1-4848-b55b-f703e8e52d73	\N	GENERAL	2026-05-15 23:21:02.692314+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان خراسان شمالی 	STUDENTS_IRAN	\N	\N	\N	\N	خراسان شمالی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
f7cdb9fe-9d1f-4002-811d-1c7548fde79c	\N	GENERAL	2026-05-15 23:21:22.413647+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان خوزستان 	STUDENTS_IRAN	\N	\N	\N	\N	خوزستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
8ada6b31-9cce-4d40-a7ba-620a085e0d0c	\N	GENERAL	2026-05-15 23:21:40.369254+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان زنجان 	STUDENTS_IRAN	\N	\N	\N	\N	زنجان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
47c659ab-4c14-4e2c-a909-3e69852c848f	\N	GENERAL	2026-05-15 23:22:00.721148+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان سمنان	STUDENTS_IRAN	\N	\N	\N	\N	سمنان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
fcefeeb3-d7b9-4c7b-af98-3fa8dbbca2e5	\N	GENERAL	2026-05-15 23:22:18.867836+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان سیستان و بلوچستان 	STUDENTS_IRAN	\N	\N	\N	\N	سیستان وبلوچستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
66565333-68c3-4bb5-8998-ca52458d01de	\N	GENERAL	2026-05-15 23:22:39.573177+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان فارس	STUDENTS_IRAN	\N	\N	\N	\N	فارس	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
6280cdfc-e0d6-4787-8ab9-fda7ad6b054f	\N	GENERAL	2026-05-15 23:23:05.236981+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان قزوین 	STUDENTS_IRAN	\N	\N	\N	\N	قزوین	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
274ed77d-b463-4975-88e5-f39e14abe324	\N	GENERAL	2026-05-15 23:23:28.189156+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان قم	STUDENTS_IRAN	\N	\N	\N	\N	قم	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
1451ae0f-037c-4b3a-a109-aaa627c68275	\N	GENERAL	2026-05-15 23:26:31.65548+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان لرستان 	STUDENTS_IRAN	\N	\N	\N	\N	لرستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
97a68ad4-24cc-4139-8bc3-97cba88c6ec3	\N	GENERAL	2026-05-15 23:26:52.838413+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان مازندران 	STUDENTS_IRAN	\N	\N	\N	\N	مازندران	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
bfbc9004-984b-4840-8f13-2c86f34b272d	\N	GENERAL	2026-05-15 23:27:10.100571+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان مرکزی	STUDENTS_IRAN	\N	\N	\N	\N	مرکزی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
b14bebdf-5f50-4455-b5fe-15c9c8d262ad	\N	GENERAL	2026-05-15 23:27:27.837665+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان هرمزگان 	STUDENTS_IRAN	\N	\N	\N	\N	هرمزگان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
5786d035-132f-4acb-8076-1e67600e1878	\N	GENERAL	2026-05-15 23:27:54.379544+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان گلستان	STUDENTS_IRAN	\N	\N	\N	\N	گلستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
86de4004-bf09-4330-b9c2-bce2d8e439d5	\N	GENERAL	2026-05-15 23:28:09.984326+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان همدان	STUDENTS_IRAN	\N	\N	\N	\N	همدان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
0ce6f2bb-1e17-4197-b6a1-ea557bd9ae36	\N	GENERAL	2026-05-15 23:28:29.986633+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان چهارمحال و بختیاری 	STUDENTS_IRAN	\N	\N	\N	\N	چهارمحال وبختیاری	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
d5a1f9d5-34ca-45d8-a62c-b616168a76c5	\N	GENERAL	2026-05-15 23:28:49.585903+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان کردستان 	STUDENTS_IRAN	\N	\N	\N	\N	کردستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
070b883b-8c6b-4ddc-8b8a-d36631ac4527	\N	GENERAL	2026-05-15 23:29:08.441633+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان کرمان	STUDENTS_IRAN	\N	\N	\N	\N	کرمان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
106dccdf-2ba6-41ab-9a94-955023b2a778	\N	GENERAL	2026-05-15 23:29:28.391091+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان کرمانشاه 	STUDENTS_IRAN	\N	\N	\N	\N	کرمانشاه	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
85e7e57d-0302-4ffc-af7d-023c6665c958	\N	GENERAL	2026-05-15 23:29:46.663968+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان یزد	STUDENTS_IRAN	\N	\N	\N	\N	یزد	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
4ba13052-0edc-4a8f-85dd-ff584811e3f9	\N	GENERAL	2026-05-15 23:30:07.494828+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان کهگیلویه و بویراحمد 	STUDENTS_IRAN	\N	\N	\N	\N	کهگیلویه وبویراحمد	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
a9359003-daf4-42e9-88ae-22f0d429311c	\N	GENERAL	2026-05-15 23:30:29.222466+00		SPECIAL	\N	\N	t	t	f	کانال دانشگاه های استان گیلان 	STUDENTS_IRAN	\N	\N	\N	\N	گیلان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
4d905533-e6de-4d05-9a6c-77c80acb9cca	\N	GENERAL	2026-05-15 23:54:20.073532+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان علوم قرآن و معارف 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
73d3479f-3d45-4283-a2e3-2b76971c30e7	\N	GENERAL	2026-05-15 23:55:57.66601+00		SPECIAL	\N	\N	t	t	f	کانال رسمی دانشجویان دانشگاه های غیرانتفاعی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	غیرانتفاعی	\N
\.


--
-- Data for Name: chat_participants; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chat_participants (chat_id, user_id) FROM stdin;
\.


--
-- Data for Name: chats; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chats (id, avatar_url, created_at, is_archived, is_muted, is_pinned, title, type, updated_at) FROM stdin;
\.


--
-- Data for Name: clubs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.clubs (id, display_order, name) FROM stdin;
54b384ea-57c5-4a3b-b2f6-f9f8f20faaab	0	کانون هلال احمر
30d7c360-1eef-49e4-bd9f-988961ff9fdb	0	کانون موسیقی
6920647a-796e-4e63-b818-51449c74255a	0	کانون تئاتر
90ead8df-b5cd-4502-a3c1-3c6264048c1e	0	کانون خیریه
\.


--
-- Data for Name: collaboration_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.collaboration_requests (id, created_at, message, responded_at, status, title, receiver_id, sender_id) FROM stdin;
\.


--
-- Data for Name: content_purchases; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.content_purchases (id, expires_at, purchased_at, content_id, transaction_id, user_id) FROM stdin;
\.


--
-- Data for Name: course_admins; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_admins (course_id, user_id) FROM stdin;
\.


--
-- Data for Name: course_chapters; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_chapters (course_id, duration_text, title, session_start_time, session_end_time) FROM stdin;
\.


--
-- Data for Name: course_collaboration_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_collaboration_requests (id, created_at, message, sender_institution_id, status, target_institution_id, updated_at, course_id) FROM stdin;
\.


--
-- Data for Name: course_collaborators; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_collaborators (course_id, collaborator_id) FROM stdin;
\.


--
-- Data for Name: course_comments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_comments (id, content, created_at, rating, reply_to_comment_id, course_id, user_id) FROM stdin;
\.


--
-- Data for Name: course_enrollments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_enrollments (id, enrolled_at, is_active, course_id, user_id, reminder_sent) FROM stdin;
\.


--
-- Data for Name: course_manual_instructors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_manual_instructors (course_id, avatar_url, name, resume) FROM stdin;
\.


--
-- Data for Name: course_materials; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_materials (id, content_type, content_url, created_at, description, is_locked, sort_order, title, course_id) FROM stdin;
\.


--
-- Data for Name: course_suitable_for; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_suitable_for (course_id, audience) FROM stdin;
\.


--
-- Data for Name: course_tags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_tags (course_id, tag) FROM stdin;
\.


--
-- Data for Name: course_teachers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.course_teachers (course_id, user_id) FROM stdin;
\.


--
-- Data for Name: courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.courses (id, admin_note, average_rating, capacity, cover_image_url, created_at, description, discount_percentage, education_level, ends_at, enrollment_limit, favorites_count, field_of_study, institution_id, is_public, is_vertical_poster, organizer_description, price_rials, review_count, scientific_association_name, slogan, starts_at, status, syllabus_duration, title, updated_at, channel_id, group_id, organizer_id, bbb_attendee_password, bbb_meeting_id, bbb_moderator_password) FROM stdin;
\.


--
-- Data for Name: discounts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.discounts (id, brand_name, category, code, created_at, description, expiry_date, image_url, percent, title) FROM stdin;
\.


--
-- Data for Name: education_levels; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.education_levels (id, created_at, display_order, has_faculty, has_field_of_study, name, role_value_en) FROM stdin;
a44bfb93-6902-4bf4-bf86-5607b0c2265e	2026-05-08 06:54:02.147551+00	1	f	f	متوسطه اول	SCHOOL_STUDENT
eb68e794-d32c-4066-8ba2-4d5307f48e60	2026-05-08 06:54:02.147558+00	2	f	t	متوسطه دوم (نظری)	SCHOOL_STUDENT
994abb9e-7e0c-4e42-b303-e01ff3e35500	2026-05-08 06:54:02.14756+00	3	f	t	هنرستان	SCHOOL_STUDENT
2e3d711b-cc98-4f59-9a2b-6e26d0d827e1	2026-05-08 06:54:02.147562+00	4	t	t	کاردانی	UNI_STUDENT
4ef70df7-f453-430c-8454-ef0ef5884bd2	2026-05-08 06:54:02.147563+00	5	t	t	کارشناسی	UNI_STUDENT
71c6f36f-9ff4-4575-ac47-b88c7195e305	2026-05-08 06:54:02.147565+00	6	t	t	کارشناسی ارشد	UNI_STUDENT
4dae1896-d5bb-440f-9b10-5df5456e49fc	2026-05-08 06:54:02.147566+00	7	t	t	دکتری	UNI_STUDENT
716fd6fa-93e3-448f-b874-f1d6c9083ada	2026-05-10 19:23:30.704974+00	8	f	f	کارشناسی ناپیوسته	UNI_STUDENT
a323dfa0-113e-4caf-85fc-d95e43dc697d	2026-05-10 22:00:51.554165+00	9	f	f	دکتری عمومی	UNI_STUDENT
\.


--
-- Data for Name: educational_role_options; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.educational_role_options (id, created_at, display_order, emoji, label_fa, value_en) FROM stdin;
ec30e105-3721-4d54-9865-e584265fc3a4	2026-05-08 06:54:02.059614+00	1	🎒	دانش‌آموز	SCHOOL_STUDENT
3f854c67-9334-492a-a1c2-695add354dc1	2026-05-08 06:54:02.059621+00	2	🎓	دانشجو	UNI_STUDENT
c3288782-81f9-45d9-bac3-45aa3099243b	2026-05-08 06:54:02.059623+00	3	👨‍🏫	استاد/معلم	TEACHER
2b6c2be8-6d0b-462f-928e-7cdfa45a3e0a	2026-05-08 06:54:02.059627+00	4	💼	آزاد	FREELANCER
\.


--
-- Data for Name: elm_events; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.elm_events (id, created_at, date, description, image_url, is_approved, is_external, link, location, organizer, reward, submitted_by_user_id, title, type) FROM stdin;
0ea4ebde-42ab-4028-af53-39d4e67fe1df	2026-05-08 06:54:06.167519+00	۱۵ اسفند ۱۴۰۲	بزرگترین چالش پردازش تصویر و داده‌های کلان در سطح کشور ویژه دانشجویان فنی.	\N	f	f	\N	دانشگاه صنعتی شریف	انجمن علمی کامپیوتر	۵۰ میلیون تومان جایزه نقدی	\N	مسابقات ملی هوش مصنوعی (شریف)	COMPETITION
38232b28-a7ff-416d-99a7-13375ba409e5	2026-05-08 06:54:06.16754+00	۲۰ فروردین ۱۴۰۳	جذب سرمایه برای طرح‌های نوآورانه در حوزه انرژی‌های تجدیدپذیر و بهینه‌سازی.	\N	f	f	\N	دانشگاه تهران - پردیس فنی	شتاب‌دهنده انرژیک	حمایت مالی تا سقف ۵۰۰ میلیون	\N	استارتاپ ویکند تخصصی انرژی	STARTUP
14f0fe18-f236-434d-875c-c813bbd9a596	2026-05-08 06:54:06.167588+00	۲۵ اردیبهشت ۱۴۰۳	ارائه جدیدترین یافته‌های دانشمندان برتر جهان در حوزه نانوپزشکی و الکترونیک.	\N	f	t	https://nano-congress2024.ir	مرکز همایش‌های بین‌المللی برج میلاد	ستاد ویژه توسعه نانو	\N	\N	کنگره بین‌المللی نانوتکنولوژی	CONGRESS
6fab16d5-5db6-45ac-a825-b67e7727abcc	2026-05-08 06:54:06.167602+00	۱۰ خرداد ۱۴۰۳	گرد هم‌آیی اساتید و دانشجویان مقاطع تحصیلات تکمیلی برای هم‌اندیشی در مبانی ریاضیات.	\N	f	f	\N	دانشگاه صنعتی اصفهان	انجمن ریاضی ایران	\N	\N	بیست و پنجمین سمینار ریاضی ایران	CONGRESS
e90703e6-cd29-4b11-9928-c0035d9fb980	2026-05-08 06:54:06.167617+00	۵ تیر ۱۴۰۳	مسابقه ایده‌پردازی و برنامه‌نویسی در لبه تکنولوژی‌های مالی و بلاک‌چین.	\N	f	f	\N	کارخانه نوآوری آزادی	بانک مرکزی (رگ‌تک)	استخدام در شرکت‌های برتر	\N	جشنواره فین‌تک برای همه	COMPETITION
\.


--
-- Data for Name: entertainment_movies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.entertainment_movies (id, created_at, description, duration, is_active, release_date, thumbnail_url, title, video_url) FROM stdin;
\.


--
-- Data for Name: entertainment_music; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.entertainment_music (id, artist, audio_url, cover_url, created_at, duration, is_active, title) FROM stdin;
\.


--
-- Data for Name: entertainment_riddles; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.entertainment_riddles (id, correct_answer_index, created_at, description, is_active, is_multiple_choice, question, reward, title, type) FROM stdin;
\.


--
-- Data for Name: event_reports; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.event_reports (id, created_at, date, description, is_verified, link, location, points_awarded, title, user_id) FROM stdin;
\.


--
-- Data for Name: exam_access_rules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_access_rules (id, channel_id, rule_type, user_id, exam_id) FROM stdin;
\.


--
-- Data for Name: exam_answers; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_answers (id, answer_text, graded_at, graded_by, is_correct, score, selected_option, attempt_id, question_id) FROM stdin;
\.


--
-- Data for Name: exam_attempts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_attempts (id, auto_score, device_info, duration_seconds, final_score, ip_address, is_submitted, manual_score, started_at, submitted_at, exam_id, user_id) FROM stdin;
\.


--
-- Data for Name: exam_question_options; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_question_options (id, is_correct, option_label, option_text, sort_order, question_id) FROM stdin;
\.


--
-- Data for Name: exam_questions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exam_questions (id, correct_answer, created_at, image_url, points, question_text, question_type, sort_order, exam_id) FROM stdin;
\.


--
-- Data for Name: exams; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.exams (id, channel_id, course_id, created_at, description, duration_minutes, ends_at, is_public, max_attempts, pass_score, show_results_after, shuffle_options, shuffle_questions, starts_at, status, title, total_score, updated_at, creator_id) FROM stdin;
\.


--
-- Data for Name: faculties; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.faculties (id, created_at, display_order, education_level, name) FROM stdin;
da10b4d4-98d6-4d88-97c1-042f15898259	2026-05-10 14:14:52.128826+00	1	\N	ادبیات و علوم انسانی
04989661-9d10-465a-abfd-25f8471b92fe	2026-05-10 14:15:21.687621+00	2	\N	الهیات و معارف اهل بیت (ع)
347c43d8-86a1-4a55-92e9-ac4a2a897808	2026-05-10 14:15:40.372928+00	3	\N	ریاضی و آمار
5f53b617-4d6e-4b87-a9ab-138a3994d6a2	2026-05-10 14:16:18.248827+00	5	\N	شیمی
66dfe162-366a-40df-8dce-6a0320f00c91	2026-05-10 14:17:10.540814+00	6	\N	علوم اداری و اقتصاد
accb64c0-f951-4b81-b953-4e2a452e515f	2026-05-10 14:49:18.221286+00	7	\N	علوم تربیتی و روانشناسی
1a713733-70f2-41a1-ba3e-5a67241679e4	2026-05-10 14:49:52.938127+00	8	\N	علوم جغرافیایی و برنامه ریزی
2c6af64c-444e-45fe-a5f8-3c1949491d54	2026-05-10 14:50:27.363032+00	10	\N	علوم ورزشی
8b82585b-88f2-4b01-aac8-5fbf7c8ea04b	2026-05-10 14:55:12.509691+00	12	\N	فنی و مهندسی
65e36b07-5af6-4198-a107-339781691260	2026-05-10 14:55:33.484265+00	13	\N	فیزیک
383e06e6-8edf-450e-8198-a8f75566b0f0	2026-05-10 14:58:44.647142+00	14	\N	مهندسی عمران و حمل و نقل
9f26d4b6-e3a6-4bbc-9e5e-4b72d04ffd7b	2026-05-10 15:01:29.379818+00	15	\N	مهندسی کامپیوتر
e3c2f4e6-e31d-412c-9f7f-8243b2b8ed57	2026-05-10 16:03:26.813137+00	16	\N	ریاضی و کامپیوتر پردیس خوانسار
fbb71b58-5d9a-41da-bfcb-9859d1da2d48	2026-05-10 19:16:30.790281+00	17	\N	مرکز آموزش عالی پردیس شهرضا
8cce7bf0-05b8-43eb-9cb4-cee32b706ff0	2026-05-10 19:17:20.28174+00	18	\N	پزشکی
e82c1b26-3a3f-4f61-ab14-2bb8a4b28940	2026-05-10 19:17:37.686752+00	19	\N	بین الملل
4f62ff2d-743b-420a-9c03-904f391c5d31	2026-05-10 19:19:13.021916+00	21	\N	توانبخشی
bdbd716a-29c5-4f4a-90f6-f09f9241b9af	2026-05-10 19:19:47.820679+00	22	\N	مدیریت و اطلاع رسانی پزشکی
7c20f80b-86c4-4a29-8c0a-1bc35155058c	2026-05-10 19:20:20.139955+00	23	\N	پیراپزشکی
f4a83d8e-c9d3-4b74-8dea-5c32e3cb16a8	2026-05-10 19:21:11.937389+00	24	\N	بهداشت
72fcad76-2be9-49c4-9fe9-86bdda3fbf4f	2026-05-10 23:03:13.600865+00	20	\N	پرستاری و مامایی
51eff9d2-a48e-41e0-901b-7b80c99609df	2026-05-15 23:50:52.904156+00	11	\N	علوم و فناوری های زیستی
2bb693a6-2f65-4485-a566-ca3fcafa31b8	2026-05-16 14:38:41.609596+00	9	\N	علوم پایه
dc776ea0-e05b-40a1-bdcb-2ff46025c934	2026-05-16 14:40:59.515163+00	4	\N	زبان های خارجی
6ffc36c2-a157-49e6-ba57-8e8e33085182	2026-05-16 15:59:19.243374+00	26	\N	علوم
3821e2e2-bd7a-4a80-b860-4fc7e086fe52	2026-05-16 21:13:30.204184+00	25	\N	ادبیات و زبان های خارجی
\.


--
-- Data for Name: feedbacks; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.feedbacks (id, admin_note, created_at, description, rating, status, title, user_id) FROM stdin;
\.


--
-- Data for Name: fields_of_study; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.fields_of_study (id, created_at, display_order, education_level, name) FROM stdin;
4b3e3be9-bada-45b1-97c4-92d0228175ef	2026-05-09 22:37:51.188677+00	1	کارشناسی	تاریخ
22c45abe-85ea-42b4-b76f-509c36952c74	2026-05-09 22:39:04.260078+00	2	کارشناسی	زبان و ادبیات فارسی
160d15ab-d117-440a-a342-640f3840786d	2026-05-09 22:39:31.364918+00	3	کارشناسی	جامعه شناسی
5243fbd4-5d38-48df-b339-1f4fedcf2d0c	2026-05-09 22:40:04.232133+00	4	کارشناسی	مددکاری اجتماعی
1f495a55-e042-472f-b3cf-7704d1688309	2026-05-09 22:40:30.762145+00	5	کارشناسی	فلسفه
462f6971-0a62-439e-ab53-0f3a93ef396a	2026-05-09 22:42:58.670911+00	6	کارشناسی	علوم قرآن و حدیث
0d6a4754-fae6-4320-b9a1-94dbcf71cbb3	2026-05-09 22:43:30.677424+00	7	کارشناسی	فلسفه و کلام اسلامی
e62914ea-9dfb-4167-98db-646f33571442	2026-05-09 22:46:28.694111+00	8	کارشناسی	ادیان و عرفان
e3e9a251-400a-49fc-87c8-b062d9319e2e	2026-05-09 22:47:05.05611+00	9	کارشناسی	تاریخ و تمدن ملل اسلامی
c2124d41-fd21-4a8b-bedf-b1584ef86749	2026-05-09 22:47:50.258117+00	10	کارشناسی	فقه و مبانی حقوق اسلامی
2a085393-665f-4f13-afa1-cec598a3fbfe	2026-05-09 22:48:42.681708+00	11	کارشناسی	آمار
2379da6d-4a6f-457d-8979-572782b45e38	2026-05-09 22:49:02.580408+00	12	کارشناسی	ریاضیات و کاربرد ها
35720f0d-556d-4b51-bc85-9b6f8a310469	2026-05-09 22:49:34.114998+00	13	کارشناسی	علوم کامپیوتر
2295075f-12a4-46f0-839e-e2d4040fc780	2026-05-09 22:55:39.419741+00	14	کارشناسی	زبان و ادبیات انگلیسی
650113e4-40cb-4e1a-9169-abb99b3f5854	2026-05-09 23:18:21.56904+00	15	کارشناسی	مترجمی زبان انگلیسی
ed33779e-bfba-4824-8477-8e31306f4372	2026-05-09 23:19:51.876633+00	16	کارشناسی	آموزش زبان انگلیسی
40000329-12df-48f8-bea6-f6db38e2c6ee	2026-05-09 23:21:19.733907+00	17	کارشناسی	زبان و ادبیات عربی
a11a172b-6de1-4038-b043-77844477fb24	2026-05-09 23:21:40.009617+00	18	کارشناسی	مترجمی زبان عربی
44747bc5-b2cf-4fee-b360-d1903f15ff57	2026-05-09 23:24:35.244236+00	19	کارشناسی	زبان و ادبیات فرانسه
abb1d49d-1c02-41b0-a8b9-e7314f6a617b	2026-05-09 23:25:06.730406+00	20	کارشناسی	زبان روسی
a93d0b8c-205d-4ed3-98ca-b212c63cd96e	2026-05-09 23:25:55.018416+00	21	کارشناسی	زبان و زبان شناسی
1a53d74b-3aeb-4ae3-be68-eaa86b63d1b5	2026-05-09 23:26:30.146084+00	22	کارشناسی	زبان و ادبیات آلمانی
82fe0923-9e39-497e-af31-53e668aa9a97	2026-05-09 23:29:30.957386+00	23	کارشناسی	زبان و ادبیات ارمنی
58bff996-94a0-466a-b193-b39abd3d2237	2026-05-09 23:30:01.767658+00	24	کارشناسی	زبان چینی
826150d3-78a5-4bf4-9682-8075512f2899	2026-05-09 23:30:28.713593+00	25	کارشناسی	شیمی محض
a04961c6-b0b0-44da-b8eb-94b509846cca	2026-05-10 13:27:59.410624+00	26	کارشناسی	شیمی کاربردی
31191b02-e9fb-40ec-8cad-ac042fcb794f	2026-05-10 13:29:04.157927+00	27	کارشناسی	اقتصاد
1863ccf6-637b-46c5-972c-aa0ee4eb4e54	2026-05-10 13:29:21.250639+00	28	کارشناسی	حسابداری
630027dc-8d9f-4154-a0a5-597e7431481e	2026-05-10 13:30:29.664658+00	29	کارشناسی	حقوق
7746daf6-339d-4003-a881-5fa3d400f18a	2026-05-10 13:30:55.368474+00	30	کارشناسی	علوم سیاسی
b94dd69a-6d2f-46f9-9907-0b4fce29d28e	2026-05-10 13:33:11.342238+00	31	کارشناسی	مدیریت صنعتی
a7f1a198-2a58-4d42-8210-a756c747dbe3	2026-05-10 13:36:04.727588+00	33	کارشناسی	مدیریت دولتی
fa2926c1-b883-4f64-b356-8efe2aa3dcfa	2026-05-10 13:37:27.970867+00	34	کارشناسی	روانشناسی
0d7a798d-a119-42cb-855a-c1afd0bd1040	2026-05-10 13:39:28.522138+00	35	کارشناسی	علم اطلاعات و دانش سیاسی
b2e9ce90-b248-421e-975b-8c6415570440	2026-05-10 13:40:00.417164+00	36	کارشناسی	علوم تربیتی
92a1a60c-166e-4027-b60c-51d9393bd98d	2026-05-10 13:43:34.58105+00	37	کارشناسی	مدیریت آموزشی ( با رویکرد آموزشی )
f7cb2c84-6262-4dbe-84ad-2ac7f18ad211	2026-05-10 13:44:03.923744+00	38	کارشناسی	مشاوره
5568953f-d240-47a4-aa62-718fbfb229f2	2026-05-10 13:46:26.629788+00	39	کارشناسی	جغرافیا
61368d53-3683-4ad6-9926-9a9219ab1f05	2026-05-10 13:46:51.560492+00	40	کارشناسی	گردشگری
6ec20e6d-1934-451f-b6b9-dfb9c088e3cd	2026-05-10 13:48:25.668433+00	41	کارشناسی	زمین شناسی
5dcef101-f7f0-4914-8ceb-62e6ff805cde	2026-05-10 13:48:45.747176+00	42	کارشناسی	علوم ورزشی
8efe9d8b-60f1-4e81-9569-8c685d81f73e	2026-05-10 13:49:05.91859+00	43	کارشناسی	زیست شناسی گیاهی
6f6620c9-0625-4e31-a0e6-4e8886ebd90b	2026-05-10 13:49:23.54892+00	44	کارشناسی	زیست شناسی جانوری
34585f2c-51db-41d3-8d5e-cbb3e29b29d5	2026-05-10 13:49:43.519778+00	45	کارشناسی	میکروبیولوژی
e2e8455e-a7ea-47f8-a1b1-30af778bf467	2026-05-10 13:50:07.102015+00	46	کارشناسی	زیست شناسی سلولی و مولکولی
4baf72dd-1b28-4273-bfbd-ac53eb5b2a27	2026-05-10 13:51:04.89672+00	47	کارشناسی	زیست فناوری ( بیوتکنولوژی )
3be0095e-9c90-48cb-927d-9cdf2521e18d	2026-05-10 13:51:32.546646+00	48	کارشناسی	مهندسی برق
610c8c4e-c4f7-4ef2-9d19-0c0559dd1824	2026-05-10 13:52:13.743078+00	49	کارشناسی	مهندسی اویونیک
5cd55631-802a-4c71-aa89-c14d9a204fac	2026-05-10 13:53:44.844599+00	50	کارشناسی	مهندسی پزشکی
ea669f5e-7d94-40ae-82b7-e79f66a1a88d	2026-05-10 13:54:05.510992+00	51	کارشناسی	مهندسی شیمی
1d0b8482-add9-49d8-a280-30c7f3915987	2026-05-10 13:54:26.874491+00	52	کارشناسی	مهندسی مکانیک
d4c91881-b788-4ddd-8a2d-19b17f013d18	2026-05-10 13:54:57.980731+00	53	کارشناسی	مهندسی هوافضا
ebf0d5c5-1af1-4454-8839-4575bb2ff59c	2026-05-10 13:55:11.834199+00	54	کارشناسی	فیزیک
fd34bc62-fcf0-44f5-89a7-91471fc63e28	2026-05-10 13:55:39.140899+00	55	کارشناسی	مهندسی خط و سازه های ریلی
fcde4eae-5669-45c3-80a1-2e9e31f98138	2026-05-10 13:56:36.052086+00	56	کارشناسی	مهندسی ماشینهای ریلی
7e74373d-2a74-4de6-a513-91962c2a106d	2026-05-10 13:57:09.819859+00	57	کارشناسی	مهندسی عمران
3726e5f1-fd48-4855-ab3c-d1c72d284628	2026-05-10 13:57:34.322071+00	58	کارشناسی	مهندسی نقشه برداری
a8cf70c8-0d11-4d8f-83c4-638e325c6acf	2026-05-10 13:58:07.082576+00	59	کارشناسی	مهندسی کامپیوتر
0b7a12fb-66be-4671-8546-fb73eb989709	2026-05-10 18:59:53.495689+00	62	کارشناسی	پرستاری
d9de2c1f-d02a-4c50-a741-bb1380ffa8f3	2026-05-10 19:00:35.481269+00	63	کارشناسی	مامایی
8d88c876-4714-46ef-82f8-55a9a0b845b5	2026-05-10 19:01:44.326484+00	64	کارشناسی	فیزیوتراپی
a3f1f315-f943-4760-9e3e-6f4ff9fbc44f	2026-05-10 19:02:30.284193+00	65	کارشناسی	اعضا مصنوعی
fbc93e20-5180-4294-9bff-53218ba2898c	2026-05-10 19:03:43.264665+00	66	کارشناسی	کار درمانی
a1d5b0c6-10af-44e1-b13c-943ac1460e4f	2026-05-10 19:04:19.811933+00	67	کارشناسی	گفتار درمانی
f3e17468-ad74-4534-8b03-83bce1ce5ae0	2026-05-10 19:04:41.422452+00	68	کارشناسی	شنوایی سنجی
f357cfe1-691e-422a-b548-d13b7d6bfc07	2026-05-10 19:05:20.928477+00	69	کارشناسی	بینایی سنجی
07b80270-a66b-4b79-9497-43faec7e15ae	2026-05-10 19:06:03.809404+00	70	کارشناسی	مدیریت خدمات بهداشتی درمانی
b96a1c45-1c8b-416a-82a8-3e4f43d657c8	2026-05-10 19:06:55.432119+00	71	کارشناسی	کتابداری و شاخه پزشکی
65eb3a49-4897-4ca3-b4be-aece1061de5f	2026-05-10 19:07:35.745499+00	72	کارشناسی	فناوری اطلاعات سلامت
b1aee4b6-a053-428e-9002-ab5c5790ef7d	2026-05-10 19:08:26.380608+00	73	کارشناسی	علوم آزمایشگاهی
06950ea0-0ba4-4b17-b3a3-e4b7cd228c98	2026-05-10 19:08:51.287743+00	74	کارشناسی	رادیولوژی
6ae45b54-63a6-40b7-85f0-71a92feb9c94	2026-05-10 19:09:23.624601+00	75	کارشناسی	اتاق عمل
c97abb07-3a8b-4f09-bf58-e5eae475ae14	2026-05-10 19:10:03.043912+00	76	کارشناسی	هوشبری
3bbd6b14-0c8c-49f9-8931-223879ceee03	2026-05-10 19:24:21.420297+00	77	کارشناسی ناپیوسته	علوم آزمایشگاهی
27c4e4d0-c9c1-4c85-9c08-540fb4fe7d03	2026-05-10 19:25:35.495756+00	78	کارشناسی ناپیوسته	رادیولوژی
acd23ba1-d56a-428f-9220-7a76e95993a4	2026-05-10 22:02:44.250446+00	61	دکتری عمومی	داروسازی
0a1db035-8e67-4159-9312-5dafa4b78505	2026-05-10 19:27:01.844057+00	79	کارشناسی ناپیوسته	اتاق عمل
37e3c71e-51d9-4dab-9dd5-870db80e904b	2026-05-10 19:33:35.28429+00	81	کارشناسی	فوریت های پزشکی
bacf09aa-4697-4ade-aa52-58ded32f96cd	2026-05-11 22:38:49.252841+00	139	کارشناسی ارشد	علوم کامپیوتر گرایش الگوریتم و نظریه محاسبه
657b3ee3-4291-4acc-bdd9-4ab5a43e1aba	2026-05-11 22:39:46.899575+00	140	کارشناسی ارشد	علوم کامپیوتر گرایش محاسبات نرم و هوش مصنوعی
396209d0-2f20-42b6-90aa-e13450c76eed	2026-05-13 07:00:17.631971+00	241	کارشناسی ارشد	جغرافیا و برنامه ریزی گردشگری گرایش برنامه ریزی مقصد
7257d83d-fada-4e1e-9202-ca00ebc93030	2026-05-13 20:40:31.992128+00	400	دکتری	علوم اقتصادی گرایش اقتصاد بیمه
0f889b97-f8d7-4e75-97c9-ad425ac97733	2026-05-13 23:35:06.68708+00	502	دکتری	جرم یابی
e59e144b-75b1-4e83-8535-992f9fc95a9a	2026-05-13 23:37:30.175049+00	507	دکتری	مدرسی اخلاق اسلامی
1e26bf28-9e38-497d-a352-08c4748b6f93	2026-05-13 23:51:29.260282+00	529	دکتری	زیست فناوری
8a61c6ef-19ad-4065-95e1-be0f32d539ab	2026-05-14 23:28:59.795664+00	610	دکتری	مهندسی کامپیوتر (شبکه و رایانش)
46ec1bd9-3846-4708-9d4f-c1aaf758e49b	2026-05-15 21:49:15.72147+00	681	دکتری	باستان شناسی گرایش دوران تاریخی
10644dd4-0ee5-43b4-8d44-86f61442a046	2026-05-10 19:33:11.436137+00	80	کارشناسی ناپیوسته	هوشبری
c0676bb9-f88f-4505-93fd-5b29fb2730ba	2026-05-11 22:42:21.915479+00	141	کارشناسی ارشد	آموزش زبان انگلیسی
e04c8b4a-3001-42f9-828a-1526f6abbd2a	2026-05-13 16:59:57.255247+00	242	کارشناسی ارشد	ژئومورفولوژی گرایش ژئومورفولوژی و آمایش محیط
00609770-dae5-40bd-8989-547053b02eb2	2026-05-13 17:03:22.260549+00	245	کارشناسی ارشد	سنجش از دور و سیستم اطلاعات جغرافیایی گرایش سیستم اطلاعات جغرافیایی
1d2d3a28-2c2e-445f-9036-62d31cae428d	2026-05-13 17:07:27.896208+00	250	کارشناسی ارشد	علوم زمین گرایش زمین شناسی مهندسی
6b89e854-6655-4d9c-b354-4a24ec238ccc	2026-05-13 17:07:53.191575+00	251	کارشناسی ارشد	علوم زمین گرایش زمین شناسی زیست محیطی
bde4d43e-ea56-490b-8a20-bbf57ea7e3cb	2026-05-13 21:12:41.084086+00	403	دکتری	فیزیولوژی ورزشی
0eb59322-7dae-4203-8086-f22085a23d8b	2026-05-13 21:19:45.451713+00	408	دکتری	رفاه اجتماعی
6f0e1482-d7a7-4619-b1b4-7e0d93c2cf5d	2026-05-13 23:35:26.969712+00	503	دکتری	گردشگری
393dd8ca-7ff6-402d-85d6-d90894cc8577	2026-05-13 23:40:22.784137+00	511	دکتری	علوم زمین گرایش زمین شناسی مهندسی
393464f3-7c74-495a-87ce-9d0c3b6dd136	2026-05-13 23:41:11.196906+00	512	دکتری	شیمی گرایش شیمی فیزیک
4df69526-8b44-4113-a203-1d36db5dee10	2026-05-13 23:41:49.258146+00	513	دکتری	شیمی گرایش شیمی تجزیه
3370012f-bc51-4c1c-b668-586b34522bc8	2026-05-13 23:42:46.122846+00	514	دکتری	شیمی کاربردی
bc89c1a2-0c4a-441c-b7c9-5d102e679172	2026-05-13 23:43:05.46113+00	515	دکتری	نانوشیمی
59695411-0a36-4c22-9779-dad0f6f33565	2026-05-14 14:27:09.26302+00	611	دکتری	مهندسی فناوری اطلاعات گرایش تجارت الکترونیکی
fdb2d33d-0095-4dde-ae43-1d4240ffc32e	2026-05-15 21:51:12.168694+00	682	دکتری	تربیت بدنی و علوم ورزشی گرایش فیزیولوژی ورزشی
4104015b-2dd4-4369-9d7a-f460d4e8dbd1	2026-05-10 19:36:53.413865+00	82	کارشناسی ناپیوسته	بهداشت عمومی
9d53fe17-0e1f-4726-a9a3-e4e3bab1c1fa	2026-05-11 22:44:21.463445+00	142	کارشناسی ارشد	مترجمی زبان انگلیسی
6968125e-38e1-4afb-a5bf-2f88ed77c476	2026-05-13 17:01:09.558438+00	243	کارشناسی ارشد	آب و هوا شناسی گرایش آب و هوا شناسی محیطی
569eb9af-f2f6-4e37-9e47-3ebda96a07d0	2026-05-13 18:52:10.899729+00	310	کارشناسی ارشد	مهندسی صدا
cd6bd62b-6637-4589-8d0e-70fca4b45924	2026-05-13 18:52:47.074951+00	311	کارشناسی ارشد	قرآن کاوی رایانشی
7c0347d4-316e-480f-9afa-cbeff1c77553	2026-05-13 21:14:29.985851+00	404	دکتری	آسیب شناسی ورزشی و حرکات اصلاحی
7923dcbd-5f6c-4edd-8d0c-2611f23a7a91	2026-05-13 21:15:16.786805+00	405	دکتری	رفتار حرکتی
2f7ef2e9-fffb-498a-b710-67a64fe3518b	2026-05-13 21:17:38.814934+00	407	دکتری	روان شناسی ورزشی
aff68894-0b84-476f-8392-cbde59a6beff	2026-05-13 21:21:06.804616+00	410	دکتری	جمعیت شناسی
a64dcac1-c3ca-46d2-8543-20821a0b4d2b	2026-05-13 23:35:44.869524+00	504	دکتری	حسابداری
427a8476-29d5-45bc-9bf6-e73649d95c56	2026-05-14 14:27:47.761012+00	612	دکتری	مهندسی فناوری اطلاعات گرایش امنیت اطلاعات
01e2cd2b-f088-4566-95a0-7588cbb482ab	2026-05-14 14:28:27.44716+00	613	دکتری	مهندسی شیمی
6a5b8503-0c41-429c-855c-f5b1ce8b90af	2026-05-16 07:57:38.284751+00	683	دکتری	علوم جغرافیایی گرایش ژئومورفولوژی
71a12d39-c58f-455b-9612-b07599e8f3cd	2026-05-10 19:38:05.427166+00	83	کارشناسی ناپیوسته	بهداشت حرفه ای
08e978bf-9bcf-409b-b68a-9cfdf9ec7c25	2026-05-10 19:38:31.611695+00	84	کارشناسی ناپیوسته	بهداشت محیط
bd3bc10c-5088-4f00-a672-2ef9df44d84e	2026-05-11 22:45:35.804585+00	143	کارشناسی ارشد	زبان و ادبیات انگلیسی
5756d260-5757-46c6-8a4a-c7704887b638	2026-05-13 17:02:29.559948+00	244	کارشناسی ارشد	آب و هوا شناسی گرایش سینوپتیک
3b4ca69e-6c9a-4f83-bb4e-8b63b1a4f626	2026-05-13 17:03:58.048895+00	246	کارشناسی ارشد	سنجش از دور و سیستم اطلاعات جغرافیایی گرایش سنجش از دور
bc736b6e-edf0-4736-8306-916e13d695fd	2026-05-13 17:09:54.915482+00	253	کارشناسی ارشد	علوم زمین گرایش زمین شناسی اقتصادی
58861781-2f08-4ba9-a933-0acdfb06c214	2026-05-13 17:13:59.216811+00	254	کارشناسی ارشد	علوم زمین گرایش زمین ساخت (تکتونیک)
152a08d6-0bf3-40cc-8dd4-fdea61ca6bef	2026-05-13 17:14:16.429991+00	255	کارشناسی ارشد	علوم زمین گرایش آب زمین شناسی
468f624a-3ffe-4cfa-9018-0ea1a5d7d08a	2026-05-13 18:55:32.290647+00	312	کارشناسی ارشد	مهندسی برق گرایش سیستم قدرت
bede108d-4d19-4012-8c60-ac345402b69e	2026-05-13 18:57:55.173367+00	316	کارشناسی ارشد	مهندسی برق گرایش مدار های مجتمع الکترونیک
feda7a1e-fa62-49a5-8d2b-0784f813a2ad	2026-05-13 18:58:43.907352+00	317	کارشناسی ارشد	مهندسی برق گرایش سیستم های الکترونیک دیجیتال
2d860df6-9538-43e4-b968-b790a4636c97	2026-05-13 21:17:19.479378+00	406	دکتری	بیومکانیک ورزشی
939e7f11-012a-490f-b0f5-40b96460ea4a	2026-05-13 21:21:37.460958+00	411	دکتری	حکمرانی جمعیت و خانواده
afb6d260-6a58-4884-b728-876615fc7aec	2026-05-13 21:22:17.270243+00	412	دکتری	مددکاری اجتماعی
8efa6595-8c2e-43db-a743-29792008557b	2026-05-13 21:23:32.992418+00	414	دکتری	تفسیر تطبیقی
7ac15810-7314-41d6-b086-71b27b058ba9	2026-05-13 21:25:19.649336+00	417	دکتری	تاریخ و تمدن ملل اسلامی
e3aaddce-7bf7-4e25-af9e-2f789ca9ea2b	2026-05-13 23:36:55.304719+00	506	دکتری	باستان شناسی گرایش پیش از تاریخ
41a4dfef-5f9e-463e-bf64-2dab7adf2fb7	2026-05-13 23:43:47.417539+00	516	دکتری	شیمی گرایش شیمی آلی
dae0a5b9-e334-4f64-96aa-0dab654333be	2026-05-13 23:44:52.427094+00	518	دکتری	شیمی گرایش شیمی پلیمر
13e2b1b3-5ec1-44b8-a349-4e8823837c96	2026-05-13 23:45:41.147292+00	519	دکتری	فیتوشیمی
fedc0f99-ff57-48dd-b5a5-07932146172d	2026-05-13 23:47:08.497744+00	522	دکتری	زیست شناسی گیاهی گرایش سیستماتیک
65c43358-d0ce-49de-b683-145919b858a6	2026-05-13 23:47:59.063603+00	523	دکتری	زیست شناسی گیاهی گرایش سلولی و تکوینی
60cb6083-0244-4fd5-8135-ce9255641ba4	2026-05-13 23:48:48.081143+00	524	دکتری	زیست شناسی جانوری گرایش فیزیولوژی
d5626529-f8e3-4562-87c7-b6c5ac6f7290	2026-05-14 21:49:05.564488+00	615	دکتری	زبان های باستانی ایران
4777edfe-26ef-4601-817b-e638103247b7	2026-05-16 07:59:35.031013+00	684	دکتری	علوم جغرافیایی گرایش طبیعی/اقلیم شناسی
f195c499-3bf5-4fef-a268-40721748b816	2026-05-16 08:01:17.704647+00	686	کارشناسی	تربیت بدنی و علوم ورزشی
73b08ac0-a19c-4763-8c8e-f4e5b701bae1	2026-05-10 22:01:39.478702+00	60	دکتری عمومی	پزشکی
913e50d9-2a17-4420-b0ab-e72fbfc45344	2026-05-12 17:58:52.122803+00	144	کارشناسی ارشد	ادبیات عربی
454b3560-c112-425b-be6e-03d75364a2d3	2026-05-13 17:04:36.185654+00	247	کارشناسی ارشد	سنجش از دور و سیستم اطلاعات جغرافیایی گرایش مطالعات دفاعی
4a9296d5-f6ed-4c00-b551-82627061a2df	2026-05-13 17:06:12.556508+00	248	کارشناسی ارشد	علوم زمین گرایش چینه نگاری و دیرینه شناسی
5199ae3c-d9c4-43a9-a6f0-b67f267e2236	2026-05-13 18:56:16.473764+00	313	کارشناسی ارشد	مهندسی برق گرایش الکترونیک قدرت و ماشین های الکتریکی
e5d0913b-dd5f-44e1-8981-6622cf6e07bd	2026-05-13 21:20:16.952462+00	409	دکتری	مردم شناسی
b169b2a5-39ce-47b7-a627-2589dbfc8bc3	2026-05-13 23:38:43.289116+00	508	دکتری	علوم زمین گرایش پترولوژی
64327ef0-2a21-4076-b42c-82a71d6e27da	2026-05-14 21:49:44.582434+00	616	دکتری	آموزش زبان فارسی به غیر فارسی زبانان
a89b0bdb-8213-4279-844a-b729b835534a	2026-05-16 08:00:05.39982+00	685	کارشناسی	روانشناسی عمومی
003ba0b2-8108-4b0b-a753-8fd9ef397195	2026-05-16 08:04:14.119266+00	687	کارشناسی	آب و هوا شناسی
f62a7b97-afda-457d-b4cd-9f758c19e06f	2026-05-10 22:27:02.386385+00	85	کارشناسی ارشد	تاریخ گرایش تاریخ ایران اسلامی
270da14a-88f9-4f23-97d8-3e3ac79a421b	2026-05-12 17:59:08.216219+00	145	کارشناسی ارشد	مترجمی زبان عربی
ed4c5b76-e84c-4256-9e8e-4eee4e78c981	2026-05-12 17:59:34.68042+00	146	کارشناسی ارشد	آموزش زبان عربی
d16f4f61-90d6-4c07-b03a-521a7802a309	2026-05-13 17:06:48.949759+00	249	کارشناسی ارشد	علوم زمین گرایش پترولوژی
0015958f-d601-46fd-aca6-67b00a1ff991	2026-05-13 17:18:42.049355+00	259	کارشناسی ارشد	بیومکانیک ورزشی
435d45d4-18fa-4130-a461-830910ec7f77	2026-05-13 17:19:53.934041+00	260	کارشناسی ارشد	فیزیولوژی ورزشی گرایش فیزیولوژی ورزشی و تندرستی
a24271be-4534-48a6-ba44-ce82df302af5	2026-05-13 17:21:08.611098+00	262	کارشناسی ارشد	فیزیولوژی ورزشی گرایش فیزیولوژی و تغذیه ورزشی
90c9a3aa-3495-487b-81e3-536375323afd	2026-05-13 18:56:43.356565+00	314	کارشناسی ارشد	مهندسی برق گرایش مخابرات سیستم
3316fb34-ced4-4172-a5f3-3bd8f87aa96b	2026-05-13 18:57:14.184667+00	315	کارشناسی ارشد	مهندسی برق گرایش کنترل
09ca72ce-6b48-4a52-9d04-18793e3430ac	2026-05-13 21:22:48.748206+00	413	دکتری	علوم قرآن و حدیث
7659ca9d-b4f9-4d9f-9db3-175ebc81fdc4	2026-05-13 21:24:07.498793+00	415	دکتری	فقه و مبانی حقوق اسلامی
e3342328-3527-44aa-b36b-7ecff31a8f94	2026-05-13 23:44:28.501765+00	517	دکتری	شیمی گرایش شیمی معدنی
e716f73b-9451-43d4-850f-2b63baa903f8	2026-05-13 23:45:59.317134+00	520	دکتری	هواشناسی
8bc0aa1a-4cc3-416a-b820-39d065b8dafa	2026-05-13 23:51:05.049158+00	528	دکتری	زیست شناسی سلولی و مولکولی
830939d2-e20f-4096-9399-fb31246f1b19	2026-05-13 23:51:58.576658+00	530	دکتری	بیوشیمی
447d3004-025e-4988-a79e-36c491176800	2026-05-14 21:50:17.428404+00	617	دکتری	علوم شناختی گرایش زبان شناسی
f62f109e-dc6b-409c-9124-3a9ebc38f40d	2026-05-14 21:50:56.238105+00	618	دکتری	ترجمه
55d27df5-078a-4fdb-9497-f4fa94b821a4	2026-05-14 21:51:41.739089+00	619	دکتری	زبان و ادبیات انگلیسی
1fca3713-781f-4811-9d28-b2888d148320	2026-05-14 21:56:56.948881+00	621	دکتری	تئاتر
e4ef9211-fd66-4ab5-828d-b5dc8e605dfd	2026-05-16 08:05:06.157541+00	688	کارشناسی	ژئومورفولوژی
80b76c22-c68d-485d-a579-07835c037cb4	2026-05-16 08:06:08.75703+00	689	کارشناسی	الهیات و معارف اسلامی گرایش ادیان و عرفان
e7f992e4-6255-4723-b120-f3be851f4bf0	2026-05-16 08:08:24.162371+00	693	کارشناسی	صنایع دستی
ebc73f60-158a-4f05-b599-c6783f6c9a36	2026-05-10 22:41:48.303935+00	86	کارشناسی ارشد	تاریخ گرایش تاریخ اسلام
c8a15bdb-2f1c-46bd-9145-8fcb870de01a	2026-05-12 18:01:20.636506+00	147	کارشناسی ارشد	زبان و ادبیات فرانسه
bad513be-3a11-484f-8904-48e6764a692b	2026-05-12 18:02:04.989076+00	148	کارشناسی ارشد	آموزش زبان فرانسه
bc467ae4-00af-417e-875d-232d6fd5f0a6	2026-05-12 18:02:40.560883+00	149	کارشناسی ارشد	مترجمی زبان فرانسه
d6f6f9e3-4d13-4a8c-b0a7-2c1558044553	2026-05-12 18:11:13.779528+00	153	کارشناسی ارشد	آموزش زبان فارسی به غیر فارسی زبانان
f07798fe-ae85-4462-abf9-6bb1cf3fd638	2026-05-12 18:12:03.07045+00	154	کارشناسی ارشد	زبان شناسی رایانشی
9f90fde5-9ad9-4edd-969d-bf8cbd8066d1	2026-05-12 18:12:33.487443+00	155	کارشناسی ارشد	شیمی گرایش شیمی آلی
6355b599-d8a1-43bf-8f56-2da2507aec2e	2026-05-13 17:09:24.576549+00	252	کارشناسی ارشد	علوم زمین گرایش رسوب شناسی و سنگ شناسی رسوبی
cc9077c5-6c73-410d-8440-5f5b1dbc2406	2026-05-13 17:15:29.895637+00	256	کارشناسی ارشد	آسیب شناسی ورزشی و تمرینات اصلاحی گرایش تمرینات اصلاحی
aa14233f-0ff9-4af8-899f-073bb5cd1d11	2026-05-13 17:16:04.442723+00	257	کارشناسی ارشد	آسیب شناسی ورزشی و تمرینات اصلاحی گرایش آسیب شناسی ورزشی
03d04361-496d-4b08-87ea-223ef3ba868e	2026-05-13 17:17:50.960132+00	258	کارشناسی ارشد	آسیب شناسی ورزشی و تمرینات اصلاحی گرایش تربیت بدنی سازگارانه و ورزش معلولین
d9445250-ec84-4c20-aaf2-9826c32561be	2026-05-13 19:02:53.566612+00	318	کارشناسی ارشد	مهندسی برق گرایش برنامه ریزی و مدیریت سیستم های انرژی الکتریکی
12d8da68-f20b-4617-8f44-91040f4bbb8f	2026-05-13 19:06:32.987946+00	322	کارشناسی ارشد	مهندسی هوا فضا گرایش فناوری ماهواره
fa0ca2de-c01c-491c-835c-f3434a896421	2026-05-13 19:07:28.427832+00	323	کارشناسی ارشد	حکمرانی انرژی
4aa93c4b-3b78-4852-9c45-880fad01c41f	2026-05-13 19:11:49.993926+00	328	کارشناسی ارشد	مهندسی شیمی گرایش فرآیند های جداسازی
893758c1-9a55-4a96-9f4d-82cbe7ab7e0c	2026-05-13 19:13:14.445412+00	329	کارشناسی ارشد	مهندسی شیمی گرایش طراحی فرآیند
d20f8f16-1afa-481d-babf-e828c35009b3	2026-05-13 19:19:18.758157+00	336	کارشناسی ارشد	مهندسی صنایع گرایش سیستم های تولید و خدمات
5cca14bf-2e53-4f2b-97bd-f5411be3e6d2	2026-05-13 21:24:31.245557+00	416	دکتری	ادیان و عرفان
a8034813-11b8-4434-b3b3-2ac866d315f4	2026-05-13 23:46:41.611979+00	521	دکتری	زیست شناسی گیاهی گرایش فیزیولوژی
bb3f0e9e-2456-44fc-8910-08db7f2c379a	2026-05-13 23:50:30.164767+00	527	دکتری	زیست شناسی دریا گرایش جانوران دریایی
84749ec0-8697-490e-870c-57ef0d6b1f4d	2026-05-13 23:58:02.86417+00	539	دکتری	ریاضی گرایش جبر
a4faa830-9a92-4a04-97b6-fca0af0b4840	2026-05-13 23:58:51.826317+00	540	دکتری	ریاضی گرایش هندسه - توپولوژی
76a541ab-48fb-41fc-ad50-43bf6c68c4c2	2026-05-13 23:59:48.624023+00	542	دکتری	آموزش ریاضی
7c697313-5781-4667-a88b-ff11757a718d	2026-05-14 21:55:34.21445+00	620	دکتری	آموزش زبان انگلیسی
ddabf6d2-37a0-4a2f-974b-0fc61a95b971	2026-05-16 08:06:41.958817+00	690	کارشناسی	باستان شناسی
c74b4d65-c961-4069-ba39-ba74a2cb3204	2026-05-10 22:42:44.149531+00	87	کارشناسی ارشد	تاریخ گرایش مطالعات خلیج فارس
c552f0a7-04d3-479d-8210-5aaa433e24bc	2026-05-12 18:08:04.361341+00	150	کارشناسی ارشد	ادبیات تطبیقی فارسی _ عربی
eded46d6-1ac0-4922-bf61-c8bacaf0289b	2026-05-12 18:08:44.73045+00	151	کارشناسی ارشد	مطالعات جهان گرایش مطالعات فرانسه
72224c6f-39b8-4cd1-89b6-608cdb61ab41	2026-05-13 17:20:18.298594+00	261	کارشناسی ارشد	فیزیولوژی ورزشی گرایش فیزیولوژی ورزشی کاربردی
f5b27b83-54eb-4ef6-9fdc-7efa1e2564c4	2026-05-13 17:24:45.027841+00	263	کارشناسی ارشد	مدیریت ورزشی گرایش مدیریت سازمان ها و باشگاه های ورزشی
06dedc07-7c1f-4917-951b-6949e77dd08b	2026-05-13 17:25:22.979873+00	264	کارشناسی ارشد	مدیریت ورزشی گرایش مدیریت رویداد ها و گردشگری ورزشی
afbb0a38-545d-49b4-8ea7-5f12a3092c20	2026-05-13 17:29:03.573042+00	268	کارشناسی ارشد	زیست شناسی گیاهی گرایش سیستماتیک و بوم شناسی
0787de38-50ec-4a19-ae59-915ff13b4937	2026-05-13 19:04:43.804674+00	319	کارشناسی ارشد	مهندسی مکاترونیک
3c9b42a3-8ae9-4131-b11b-305176e74779	2026-05-13 21:25:43.350797+00	418	دکتری	حکمت متعالیه
f5e74709-aace-4585-bc37-aee66d4a2d49	2026-05-13 21:27:47.353055+00	420	دکتری	فلسفه و کلام اسلامی
e6fb0af0-4ab2-4042-a743-34bfafbe87a4	2026-05-13 21:28:09.451335+00	421	دکتری	مذاب کلامی
6a7191eb-04e0-483c-add0-c4209b7c0fc3	2026-05-13 23:49:23.414349+00	525	دکتری	زیست شناسی جانوری گرایش بیوسیستماتیک
e863fe00-8a38-4c76-bbf6-ce6934139539	2026-05-13 23:53:34.350661+00	531	دکتری	ژنتیک مولکولی
3ca665cc-2d52-411e-a751-9a03d3c553ca	2026-05-13 23:54:18.060176+00	532	دکتری	میکروبیولوژی
742bb608-bf95-42b2-8727-089cdef571eb	2026-05-13 23:54:45.509867+00	533	دکتری	بیوفیزیک
be63e5dc-fe8c-45d3-88df-e8603aace7f5	2026-05-14 21:57:21.649125+00	622	دکتری	پژوهش هنر
5f099fca-45c1-4a38-97bf-7cb2d5b73e86	2026-05-16 08:07:18.413488+00	691	کارشناسی	راهنمایی و مشاوره
9ee41cd9-e28c-48f9-a730-49fd27cdf4cf	2026-05-16 08:07:52.453595+00	692	کارشناسی	روانشناسی بالینی
47dbd80f-0ce5-4ecf-ac73-f1d2ad3769a4	2026-05-16 08:11:53.222954+00	694	کارشناسی	مدیریت جهانگردی
c4caf67a-e7e0-49dd-aaf5-e4ef8eb02aa9	2026-05-11 16:52:16.677209+00	88	کارشناسی ارشد	شیعه شناسی گرایش تاریخ
7502b641-00c3-4b35-be03-0b3a75683662	2026-05-12 18:10:04.964173+00	152	کارشناسی ارشد	زبان شناسی گرایش محض
4b36eba3-14c4-4fc4-ae45-d36be9ab6911	2026-05-12 18:12:52.158745+00	156	کارشناسی ارشد	شیمی گرایش شیمی پلیمر
87d50aca-265e-4f1f-8cf3-43f2a2ae8b80	2026-05-12 18:15:23.40141+00	159	کارشناسی ارشد	نانوفیزیک
d6335bfb-6d63-481d-a93d-1b8e3b275d29	2026-05-13 17:26:28.524674+00	265	کارشناسی ارشد	مدیریت ورزشی گرایش مدیریت بازاریابی و ارتباطات ورزشی
54854e50-1c77-4fb5-9395-3b5713ddb2a9	2026-05-13 17:27:17.989838+00	266	کارشناسی ارشد	رفتار حرکتی گرایش یادگیری و کنترل حرکتی
b32b1cc4-958b-4ffd-b8b0-6a8fe99f6686	2026-05-13 17:28:07.774075+00	267	کارشناسی ارشد	روان شناسی ورزشی
89607335-6daa-4964-a248-1706e4609f13	2026-05-13 17:35:13.368059+00	277	کارشناسی ارشد	ژنتیک
4067658c-0fca-4df9-acc4-22eda0bebc98	2026-05-13 19:05:10.176383+00	320	کارشناسی ارشد	مهندسی برق گرایش رادار
6da4695a-2ce4-423b-855c-59a162d1e737	2026-05-13 19:13:34.019192+00	330	کارشناسی ارشد	مهندسی شیمی گرایش پلیمر
9704047d-6d57-466d-b500-b7215d8bd92d	2026-05-13 19:13:51.377586+00	331	کارشناسی ارشد	مهندسی شیمی گرایش محیط زیست
99447b37-2ccd-44f7-bec6-4796b6340846	2026-05-13 19:14:21.185907+00	332	کارشناسی ارشد	مهندسی شیمی گرایش فرآوری و انتقال گاز
c6f328ce-99ae-41d3-bd73-d7d263e390aa	2026-05-13 19:15:11.063664+00	333	کارشناسی ارشد	مهندسی پلیمر گرایش فراورش
8963e544-d1c1-47d5-ad04-ce01c6a91799	2026-05-13 19:16:27.111166+00	335	کارشناسی ارشد	مهندسی شیمی گرایش ترموسینتیک و کاتالیست
3635fe0f-baad-4b6e-b471-356400db6f62	2026-05-13 21:26:08.072864+00	419	دکتری	کلام امامیه
7dbc87e7-06e5-4212-9a79-39bdfebcd9bc	2026-05-13 21:42:34.701327+00	424	دکتری	فلسفه فیزیک
56ec673c-b712-4284-b1f2-f5760eccaf1a	2026-05-13 23:49:49.023321+00	526	دکتری	زیست شناسی جانوری گرایش سلولی و تکوینی
720d0a71-b38f-40e1-aba4-9cab2f03f771	2026-05-13 23:55:07.699734+00	534	دکتری	زیست فناوری گرایش میکروبی
7067b503-35e3-4b9e-8d0c-780166c65c84	2026-05-13 23:55:55.015141+00	535	دکتری	ریز زیست فناوری (نانوبیوتکنولوژی)
377d5aaa-e39b-409f-b84e-29fe967c4bd8	2026-05-13 23:57:37.747991+00	538	دکتری	ریاضی
6ff385b7-d803-4b21-9e0b-d9e6ed463fda	2026-05-14 22:22:29.268979+00	623	دکتری	مهندسی هسته ای گرایش کاربرد پرتوها
b631e725-c7d5-47e5-98bb-f7ec0aae4872	2026-05-16 14:08:37.998445+00	695	کارشناسی	جغرافیا و برنامه ریزی شهری
962a8f01-9e84-4973-9415-ca1201772f62	2026-05-10 22:51:52.010588+00	89	کارشناسی ارشد	تاریخ فرهنگ و تمدن اسلامی
b30bcafc-1be7-44ad-ae33-617c3e2e404f	2026-05-10 22:54:12.885249+00	90	کارشناسی ارشد	تاریخ گرایش تاریخ ایران باستان
50b232e6-f53f-4a78-835c-bfd53f684c69	2026-05-10 22:54:47.82226+00	91	کارشناسی ارشد	زبان و ادبیات فارسی
428ba844-a91b-4149-ae3e-caf5564e5418	2026-05-10 22:55:25.992823+00	92	کارشناسی ارشد	زبان و ادبیات فارسی گرایش ادبیات پایداری
0ba61913-d23f-473b-96f1-c5933c8f5659	2026-05-12 18:13:10.823705+00	157	کارشناسی ارشد	شیمی گرایش شیمی تجزیه
54013b2c-b07a-4b15-9daf-a5feb0b317a1	2026-05-12 18:13:33.83283+00	158	کارشناسی ارشد	نانوشیمی
c86f02a1-e4a8-4bae-a25f-dcd38d3c05aa	2026-05-13 17:30:39.219703+00	269	کارشناسی ارشد	زیست شناسی گیاهی گرایش فیزیولوژی
6c9fd9e9-ada1-44f1-9688-1fa6b8859770	2026-05-13 17:31:37.5824+00	270	کارشناسی ارشد	زیست شناسی گیاهی گرایش سلولی و تکوینی
3785f492-4e41-425a-9c4a-c00f91ef5e54	2026-05-13 17:34:53.13175+00	276	کارشناسی ارشد	میکروبیولوژی گرایش محیطی
9faee5fe-e3de-4a56-9874-d939b58078b6	2026-05-13 19:05:47.447747+00	321	کارشناسی ارشد	مهندسی ورزش
a29e76e4-c36f-4363-8997-35d09e1f9198	2026-05-13 19:09:46.116965+00	326	کارشناسی ارشد	مهندسی پزشکی گرایش بیومتریال
a002978a-6a40-46b4-a0b1-755c0c70485d	2026-05-13 21:28:35.987773+00	422	دکتری	فقه شافعی
9ddc723e-7ecd-4735-ad1c-123ca0c35132	2026-05-13 23:56:42.827134+00	536	دکتری	آمار
a909abcf-dbd8-48b0-80fe-14c967f04fbe	2026-05-13 23:57:19.254024+00	537	دکتری	ریاضی گرایش آنالیز
66aef5a0-5261-4677-a941-0862ddbcf735	2026-05-14 00:02:12.504137+00	545	دکتری	فیزیک گرایش فیزیک پلاسما
fc5ddb57-4601-4b0c-910a-9af5b1fe380c	2026-05-14 22:24:45.798047+00	624	دکتری	مهندسی هسته ای گرایش راکتور
9c924127-b58b-4c71-b802-e8176fc18138	2026-05-16 14:15:55.585751+00	696	کارشناسی ارشد	تربیت بدنی و علوم ورزشی گرایش فیزیولوژی ورزشی
66cd68db-86d6-4069-ab33-6592899e6124	2026-05-10 22:57:33.032869+00	93	کارشناسی ارشد	زبان و ادبیات فارسی گرایش ویرایش و نگارش
6c573186-1495-4382-bc5a-c538d3b8364b	2026-05-10 23:00:02.160802+00	96	کارشناسی ارشد	جامعه شناسی
8eb92465-0ba5-4f42-a504-80db8cdd8799	2026-05-12 18:20:00.163166+00	160	کارشناسی ارشد	نانوفناوری گرایش نانومواد
abcf6f84-b2c4-4dc8-9698-672c29e58c05	2026-05-13 17:32:01.630613+00	271	کارشناسی ارشد	زیست شناسی جانوری گرایش فیزیولوژی جانوری
a7c847e8-9c49-4b06-8628-78c6bebe2b08	2026-05-13 17:32:41.120681+00	272	کارشناسی ارشد	زیست شناسی جانوری گرایش بیوسیستماتیک جانوری
62932ac3-871e-4cff-a6f6-9c5c7cc7ef46	2026-05-13 17:33:00.918662+00	273	کارشناسی ارشد	زیست شناسی جانوری گرایش سلولی و تکوینی
9a264636-af96-47a0-9312-aae3a54a866f	2026-05-13 17:33:48.269939+00	274	کارشناسی ارشد	میکروبیولوژی گرایش میکروارگانسیم های بیماری زا
7971d960-b60c-4e83-a29b-678a5d483547	2026-05-13 17:34:35.579513+00	275	کارشناسی ارشد	میکروبیولوژی گرایش صنعتی
39302961-4339-4432-9d33-3078eb82b2fd	2026-05-13 19:07:52.021839+00	324	کارشناسی ارشد	مهندسی رباتیک
40e061b5-f15a-49f7-87ea-1c905cdfaf6d	2026-05-13 19:15:56.948698+00	334	کارشناسی ارشد	مهندسی شیمی گرایش مهندسی انرژی
0b95091a-8684-4fca-b43b-e4fd2151a4af	2026-05-13 21:30:51.852018+00	423	دکتری	تاریخ علم در دوره اسلامی
cd087ec1-c1c8-4f1d-8c54-2da5b804076e	2026-05-13 21:34:47.537436+00	425	دکتری	برنامه ریزی درسی
e3351533-4ac1-4dad-88b1-7336e55fc1ee	2026-05-13 23:59:24.320204+00	541	دکتری	ریاضی گرایش کاربردی
8d03ac10-2a29-4040-8e2b-1be12d54e709	2026-05-14 00:06:35.919535+00	546	دکتری	زبان شناسی
62ef4b71-89dd-49b3-ba82-f324a753555b	2026-05-14 00:10:37.236327+00	548	کارشناسی ارشد	علوم و مهندسی باغبانی گرایش گیاهان زینتی
e0c3f2e3-b677-413c-8269-43313d3df8ea	2026-05-14 22:26:12.239123+00	625	دکتری	مهندسی هسته ای گرایش مهندسی پرتوپزشکی
8278e4bb-710d-4c48-95a9-bb333fe37774	2026-05-14 22:26:56.156405+00	626	دکتری	مهندسی هسته ای گرایش گداخت
94051fee-886e-4c67-aa9f-e62aaec0fe6f	2026-05-16 14:26:57.398869+00	697	کارشناسی ارشد	جغرافیای طبیعی گرایش اقلیم شناسی در برنامه ریزی محیطی
fe91eb6b-279e-4e55-ba7a-0733e9a1f248	2026-05-16 14:29:47.580833+00	701	کارشناسی ارشد	عرفان اسلامی
f35b2b62-1736-44b4-9506-7fba3a760792	2026-05-10 22:58:06.679001+00	94	کارشناسی ارشد	زبان و ادبیات فارسی گرایش آموزش زبان فارسی
e485d13c-cadf-4f5c-b449-bf6ee0ac126c	2026-05-12 18:21:55.486619+00	161	کارشناسی ارشد	مهندسی فناوری اطلاعات
aa134ef8-bd76-40cc-b4b7-6ec07515ec3f	2026-05-12 18:22:52.266625+00	162	کارشناسی ارشد	مهندسی فناوری اطلاعات گرایش تجارت الکترونیکی
b419449e-0bcc-4eec-b2be-0821abf702d1	2026-05-12 18:23:31.963774+00	163	کارشناسی ارشد	مهندسی فناوری اطلاعات گرایش مدیریت سیستم های اطلاعاتی
e3576305-bdae-4e9a-ac47-0da3c584936a	2026-05-12 18:24:32.80636+00	164	کارشناسی ارشد	مهندسی فناوری اطلاعات گرایش سیستم های تکنولوژی اطلاعات (ITS)
5345040f-18c9-47ec-9302-98c143cdc11f	2026-05-12 18:27:19.117399+00	166	کارشناسی ارشد	مهندسی فناوری اطلاعات گرایش سیستم های چند رسانه ای
4d6b70e0-1ed6-44fb-abd5-9597156f6ecc	2026-05-13 17:35:40.779131+00	278	کارشناسی ارشد	زیست شناسی سلولی و مولکولی
84268bfe-4a91-4703-bbff-7d54c0b6055b	2026-05-13 19:08:54.077678+00	325	کارشناسی ارشد	مهندسی پزشکی گرایش بیوالکتریک
67fbb068-d83d-480a-b1dd-a43d1e68c130	2026-05-13 19:19:46.938687+00	337	کارشناسی ارشد	مهندسی صنایع گرایش لجستیک و زنجیره تامین
a043f7f6-f854-43bd-b925-c3e354bc7026	2026-05-13 19:20:27.184391+00	338	کارشناسی ارشد	مهندسی صنایع گرایش آینده پژوهی
9d7480fe-d74d-4b66-bb84-dce0eca325d8	2026-05-13 19:21:07.922903+00	339	کارشناسی ارشد	مهندسی مکانیک گرایش طراحی کاربردی
c57a9a55-6896-49f2-997a-7b449dde7015	2026-05-13 21:35:38.19731+00	426	دکتری	مدیریت آموزشی
90c3873c-16ff-4014-8406-3b1de702c66f	2026-05-13 21:43:59.583043+00	430	دکتری	روان شناسی و آموزش کودکان استثنایی
f67c77eb-283a-422f-bda9-e088be263dc8	2026-05-13 21:47:00.078131+00	434	دکتری	روان شناسی صنعتی و سازمانی
962eec81-d0d4-4180-bc2e-a589afcc518d	2026-05-13 21:47:51.549614+00	436	دکتری	روان شناسی سلامت
c8e3c311-b252-4656-a77c-b31828a2dcf2	2026-05-14 00:00:44.116366+00	543	دکتری	فیزیک
5a875d16-1c36-4306-85dc-33316d3cbfa2	2026-05-14 00:01:20.457316+00	544	دکتری	فیزیک گرایش اپتیک و لیزر
3ea78db9-6cca-4f05-a651-1f959da30342	2026-05-14 00:09:48.054943+00	547	کارشناسی ارشد	علوم و مهندسی باغبانی گرایش سبزی ها
c217db21-20e0-4090-b2b7-df57c1d3b6fc	2026-05-14 00:11:18.111106+00	549	کارشناسی ارشد	علوم و مهندسی باغبانی گرایش گیاهان دارویی
e29f7d20-e3bb-489c-9344-957ad6ffe1f1	2026-05-14 00:11:50.943679+00	550	کارشناسی ارشد	علوم و مهندسی باغبانی گرایش تولید محصولات گلخانه ای
ff17e956-7e06-4ec3-8013-674887c67da0	2026-05-14 00:13:37.40925+00	553	کارشناسی ارشد	ترویج و آموزش کشاورزی پایدار گرایش زیست بوم انسانی کشاورزی
4d307ce7-192e-4656-9efd-c0be598d2f93	2026-05-14 22:27:34.803712+00	627	دکتری	مهندسی هسته ای گرایش چرخه سوخت
64ca34e2-2c38-4383-be89-3db9de556ace	2026-05-16 14:27:48.07739+00	698	کارشناسی ارشد	باستان شناسی
5c69490c-fd8b-4e58-86ea-f1bd6ace1bc7	2026-05-16 14:28:20.050498+00	699	کارشناسی ارشد	جغرافیای طبیعی گرایش ژئومورفولوژی
706ad19b-41f5-4085-9f60-6fc40ee1483c	2026-05-10 22:59:20.754595+00	95	کارشناسی ارشد	زبان و ادبیات فارسی گرایش ادبیات عامه
2d92472a-42fe-4b9a-8b30-e0e90391bd7c	2026-05-10 23:02:00.975698+00	97	کارشناسی ارشد	جمعیت شناسی
66ab5f0d-9242-47ab-b328-113bfddc1673	2026-05-12 18:25:43.543024+00	165	کارشناسی ارشد	مهندسی فناوری اطلاعات گرایش سامانه های شبکه ای
738255db-a8be-443b-ab07-2205c60162b0	2026-05-13 17:35:54.314793+00	279	کارشناسی ارشد	بیوشیمی
f95857c7-b1eb-43aa-bec0-2e3ff6c30995	2026-05-13 17:42:09.303398+00	287	کارشناسی ارشد	فیزیک گرایش فیزیک پلاسما
f3ebbc16-165f-49a2-b3c1-3840903270f4	2026-05-13 19:10:31.37353+00	327	کارشناسی ارشد	مهندسی پزشکی گرایش بیومکانیک
a85004db-c90c-4a7f-b97d-1f898e37096d	2026-05-13 21:39:08.547417+00	427	دکتری	تکنولوژی آموزشی
bc2f134a-d738-450c-8c60-1a120d304ded	2026-05-13 21:41:45.157862+00	428	دکتری	آموزش عالی گرایش مدیریت آموزش عالی
a670de10-4062-47a7-94ad-93a8f173ba3e	2026-05-13 21:42:53.570983+00	429	دکتری	روان شناسی تربیتی
d05f54b8-b9b7-4bc1-98e9-bc5e5db2272a	2026-05-14 00:12:31.20681+00	551	کارشناسی ارشد	ترویج و آموزش کشاورزی پایدار گرایش ترویج کشاورزی پایدار و منابع طبیعی
868dd67f-1524-403a-93c7-29fdd4e61f31	2026-05-14 00:13:03.619457+00	552	کارشناسی ارشد	ترویج و آموزش کشاورزی پایدار گرایش آموزش کشاورزی پایدار و محیط زیست
27ac1897-969a-456b-9421-d80f2b5027ac	2026-05-14 22:41:18.780655+00	628	دکتری	معماری
ca0ecd2a-70b6-41bc-882f-01dd469035f0	2026-05-16 14:29:22.765115+00	700	کارشناسی ارشد	جغرافیا و برنامه ریزی شهری
7708b138-6a73-4b2f-ad41-c108857f1c62	2026-05-11 16:48:02.908283+00	98	کارشناسی ارشد	تاریخ تمدن اسلامی
f283fdcc-41ec-47ec-9f5b-1871fb536cda	2026-05-12 18:28:25.203947+00	167	کارشناسی ارشد	مهندسی فناوری اطلاعات پزشکی
464f1126-84d7-4868-b0bf-9f8085005214	2026-05-12 18:29:11.378978+00	168	کارشناسی ارشد	مهندسی فناوری اطلاعات گرایش معماری سازمانی
93394196-b3b5-48c6-8fb5-d1d127a807e9	2026-05-12 18:34:48.787135+00	172	کارشناسی ارشد	شیمی گرایش شیمی فیزیک
4dfb4bee-9077-4916-a476-3eaf76d962d9	2026-05-13 17:36:43.964138+00	280	کارشناسی ارشد	ریز زیست فناوری (نانوبیوتکنولوژی)
787f3b5f-145b-4ced-a94d-3fd3b0dbdca7	2026-05-13 17:38:13.882512+00	282	کارشناسی ارشد	زیست فناوری گرایش مولکولی
01d91300-c53a-448d-9582-b7d5e94bf845	2026-05-13 17:39:01.473767+00	283	کارشناسی ارشد	مهندسی شیمی گرایش بیوتکنولوژی
17e09029-e0f9-436e-a200-c344c44d32c4	2026-05-13 17:39:59.089991+00	284	کارشناسی ارشد	مهندسی شیمی گرایش داروسازی
6e937a8b-841c-4c59-aa19-e97315268239	2026-05-13 19:21:52.879783+00	340	کارشناسی ارشد	مهندسی مکانیک گرایش تبدیل انرژی
d60449e6-076f-4ca7-a95e-5fd60f9a2dc8	2026-05-13 21:44:40.990506+00	431	دکتری	مشاوره
0d9b5d63-a66c-4b57-aaa5-6ed6423fd6fb	2026-05-13 21:45:24.727151+00	432	دکتری	سنجش و اندازه گیری
ac36a5c3-5a03-4e3c-a147-4faba28186fa	2026-05-14 00:15:43.001485+00	554	کارشناسی ارشد	ترویج و آموزش کشاورزی پایدار گرایش نوآوری و کارآفرینی کشاورزی
ca9e4f1d-70ce-4587-ae7e-b8590d845d93	2026-05-14 00:19:36.886121+00	556	کارشناسی ارشد	علوم و مهندسی جنگل گرایش علوم زیستی جنگل
d2cfe45d-75a5-4e47-87dd-2028294da921	2026-05-14 00:20:34.885033+00	558	کارشناسی ارشد	مدیریت حاصلخیزی و زیست فناوری خاک گرایش شیمی، حاصلخیزی خاک و تغذیه گیاه
c430c74b-5055-438f-be42-dd55b982c703	2026-05-14 00:23:05.943921+00	563	کارشناسی ارشد	علوم دامی گرایش فیزیولوژی دام و طیور
e14591e5-c4e5-4973-90be-05b912fa1180	2026-05-14 22:46:13.242567+00	629	دکتری	آموزش عالی گرایش برنامه ریزی توسعه آموزش عالی
feaf3f05-b7ce-493d-9747-fc657bf91bb0	2026-05-14 22:47:11.356116+00	630	دکتری	علوم زمین گرایش فسیل شناسی و چینه شناسی
1951e99b-aede-45db-ae9b-f0133ec1fdcc	2026-05-16 14:30:13.97425+00	702	کارشناسی ارشد	تحقیقات آموزشی
06b849df-b1e3-4b9b-8300-3c5fc34a67bd	2026-05-11 16:49:10.139872+00	99	کارشناسی ارشد	تاریخ و تمدن ملل اسلامی
36c1da54-aa8a-4363-8128-b3cdb77162b5	2026-05-11 16:50:05.609684+00	100	کارشناسی ارشد	تاریخ و تمدن اسلامی در شبه قاره هند
a1369c90-f100-47aa-bede-3ea1d0050cc2	2026-05-12 18:30:28.926648+00	169	کارشناسی ارشد	مهندسی مواد گرایش سرامیک
ad178428-e9f7-40db-bf61-15413f78617b	2026-05-12 18:31:06.126427+00	170	کارشناسی ارشد	مهندسی متالوژی و مواد گرایش الکتروسرامیک
22169a41-5461-4927-9a1b-ce7e3f2f8c5d	2026-05-13 17:37:44.639085+00	281	کارشناسی ارشد	زیست فناوری گرایش میکروبی
1aa9bf9b-5e8e-4d15-a35f-c8cbf300cb96	2026-05-13 19:22:57.518522+00	341	کارشناسی ارشد	مهندسی هوا فضا گرایش دینامیک پرواز و کنترل
d883ee1f-f3c5-4e2c-a55a-530ef0c23864	2026-05-13 19:27:04.742523+00	346	کارشناسی ارشد	مهندسی عمران گرایش منابع آب
7198475e-9cba-4258-9d6f-c6b9d8ef8fbf	2026-05-13 19:28:00.442936+00	348	کارشناسی ارشد	مهندسی عمران گرایش مهندسی و مدیریت ساخت
91d2605e-fbd6-456b-85fa-a5b1c35247dc	2026-05-13 21:46:27.21042+00	433	دکتری	روان شناسی
796adde5-13ab-4838-bb2e-3c096a696dab	2026-05-14 00:19:00.21654+00	555	کارشناسی ارشد	علوم و مهندسی جنگل گرایش مدیریت جنگل
643bbe5f-72d8-479b-ba0a-cc623d242474	2026-05-14 00:20:02.885453+00	557	کارشناسی ارشد	علوم و مهندسی جنگل گرایش عمران و بهره برداری جنگل
cca4a71d-3495-4368-8958-dc08a374208b	2026-05-14 00:21:11.498771+00	559	کارشناسی ارشد	مدیریت حاصلخیزی و زیست فناوری خاک گرایش بیولوژی و بیوتکنولوژی خاک
f71c5a8b-8773-4da4-87c1-b7d2e9e8a6bf	2026-05-14 22:47:58.109511+00	631	دکتری	علوم زمین گرایش زمین شناسی اقتصادی
3f179e47-db78-4f8d-a0ad-f8040413b99d	2026-05-16 14:48:54.044917+00	703	کارشناسی	زیست شناسی عمومی
53a17058-f3a5-4171-8d80-3aa3612ab60b	2026-05-11 16:55:02.672536+00	101	کارشناسی ارشد	شیعه شناسی گرایش جامعه شناسی
ed9843eb-1a06-4368-ade2-28154aa4f54d	2026-05-12 18:34:29.247665+00	171	کارشناسی ارشد	مهندسی انرژی های تجدیدپذیر
8ecf1502-a338-46c3-a8c1-5bceaf7b5a9b	2026-05-12 18:40:08.954118+00	176	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد و تجارت الکترونیک
74a3160a-73ce-4fc6-8677-e4e6aef2198a	2026-05-12 19:05:07.772964+00	175	کارشناسی ارشد	علوم اقتصادی گرایش توسعه اقتصادی و برنامه ریزی
1a71c820-8057-4281-9354-10112da36742	2026-05-13 17:41:17.59905+00	285	کارشناسی ارشد	فیزیک
7663090f-982d-4ffd-9c13-388b5a1e173a	2026-05-13 17:44:36.434931+00	290	کارشناسی ارشد	فیزیک گرایش نجوم و اخترفیزیک
9b777231-e81b-4017-8af7-5a21e3e7a08c	2026-05-13 17:47:56.543464+00	293	کارشناسی ارشد	فیزیک گرایش اپتیک و لیزر
24a2c692-4088-4c07-9255-9ffad4caf93d	2026-05-13 17:48:22.324261+00	294	کارشناسی ارشد	فیزیک گرایش علوم و فناوری کوانتمی
d82496b8-3657-4f16-8fe9-ff77172799f5	2026-05-13 17:49:01.039784+00	295	کارشناسی ارشد	فیزیک گرایش اتمی و مولکولی
d5d6f498-e60c-4032-a8a9-a3c1998cf5e4	2026-05-13 19:24:14.663035+00	342	کارشناسی ارشد	مهندسی عمران گرایش حمل و نقل
81ff48b4-0858-48af-9a4a-b78536446ce6	2026-05-13 19:27:32.04582+00	347	کارشناسی ارشد	مهندسی عمران گرایش مهندسی و مدیریت منابع آب
2fdd523e-4493-4e9e-999b-da5fd690e603	2026-05-13 21:47:28.342397+00	435	دکتری	روان شناسی بالینی
ace24b24-72be-4875-b272-16182b802e7b	2026-05-13 21:54:16.150354+00	440	دکتری	حقوق کیفری و جرم شناسی
7cb004a6-ad28-4fed-8672-7d1dbad6d8fa	2026-05-14 00:21:45.885271+00	560	کارشناسی ارشد	مدیریت منابع خاک گرایش فیزیک و حفاظت خاک
d5a01ba1-045c-4b35-b4ca-122aa0d8c180	2026-05-14 00:23:32.108137+00	564	کارشناسی ارشد	علوم دامی گرایش تغذیه دام
eda09cfa-103f-4094-b446-68129ac14250	2026-05-14 00:29:34.06134+00	568	کارشناسی ارشد	فناوری و كارآفرينی پايدار گرايش فناوری های نوين در منابع طبيعی
8ac1de78-ec76-4656-9392-819ce579dbea	2026-05-14 22:53:16.094441+00	632	دکتری	فیزیک گرایش فیزیک ماده چگال
abab8827-72fa-4d16-adfd-c2140f1c20bb	2026-05-14 22:54:35.82166+00	634	دکتری	فیزیک گرایش فیزیک هسته ای
1bd70309-5616-40ae-bc5f-9b69b6fb1451	2026-05-14 22:56:24.670813+00	636	دکتری	فیزیک گرایش فیزیک آماری و سامانه های پیچیده
0f3e0d88-0bf6-4af3-afcc-fe1690965dbb	2026-05-11 16:55:24.199639+00	102	کارشناسی ارشد	شیعه شناسی گرایش کلام
4cc630be-7e34-463e-a4fe-05f980be9ef0	2026-05-12 18:35:52.367802+00	173	کارشناسی ارشد	شیمی گرایش شیمی معدنی
5c4f8010-0f5e-4f3d-b79d-3884635b440d	2026-05-12 18:36:20.987689+00	174	کارشناسی ارشد	شیمی گرایش شیمی کاربردی
4348e49b-a275-4c56-8798-d67d4e02ac78	2026-05-13 17:41:52.639298+00	286	کارشناسی ارشد	فیزیک گرایش فیزیک ماده چگال
8e60fb98-570e-4759-8e05-1fc6865e0b6f	2026-05-13 17:42:42.538782+00	288	کارشناسی ارشد	فیزیک گرایش فیزیک هسته ای
eabd8e25-cc4b-4b25-be23-7311cde0dc41	2026-05-13 17:43:45.526993+00	289	کارشناسی ارشد	فیزیک گرایش ذرات بنیادی و نظریه میدان ها
a3457f80-65c9-4bf7-afb6-d2f12c135d05	2026-05-13 19:24:52.592607+00	343	کارشناسی ارشد	مهندسی عمران گرایش راه و ترابری
caff2e65-53f5-45da-a923-28a09e6ba0e0	2026-05-13 19:25:41.746023+00	344	کارشناسی ارشد	مهندسی عمران گرایش سازه
7b42c6c5-cb40-40be-b62f-1e5d3987e083	2026-05-13 19:26:30.058212+00	345	کارشناسی ارشد	مهندسی عمران گرایش ژئوتکنیک
b6f30696-2f9a-4bbb-a55e-5f531b6e29e9	2026-05-13 21:49:24.075874+00	437	دکتری	علوم شناختی - روانشناسی گرایش روان شناختی
e27e785a-41fb-48dc-955f-03b19f00bd4e	2026-05-13 21:54:39.499455+00	441	دکتری	فقه و حقوق جزا
3a6f14f2-c542-4db9-9313-4d906de269a9	2026-05-14 00:22:13.431586+00	561	کارشناسی ارشد	مدیریت منابع خاک گرایش منابع خاک و ارزیابی اراضی
d7012187-7426-4abe-b4e7-801dd8b4730c	2026-05-14 22:53:49.934144+00	633	دکتری	فیزیک گرایش اتمی و مولکولی
224e2a63-56d2-4d24-bb18-0e3a27e969c3	2026-05-14 22:55:21.77365+00	635	دکتری	فیزیک گرایش نجوم و اخترفیزیک
2486a300-b123-4bd7-8b4f-a67262203981	2026-05-11 17:13:14.190571+00	103	کارشناسی ارشد	تاریخ علم گرایش طب و داروسازی در جهان اسلام
6db33e58-5afc-43cc-b421-e4777e2f4796	2026-05-11 17:14:57.313054+00	104	کارشناسی ارشد	تاریخ علم گرایش ریاضی در جهان اسلام
66599c66-7e71-4302-aeb7-c2c73d58751b	2026-05-12 19:05:47.569529+00	177	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد اسلامی
3862cf75-0722-43ca-8b29-f80b285fbc20	2026-05-12 19:06:09.050348+00	178	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد انرژی
b07d046c-1386-4372-a9da-0d3031e3a799	2026-05-12 19:08:56.69398+00	182	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد شهری
ba7af7c4-1535-4bb6-bfb1-4e7119d572a9	2026-05-13 17:45:02.080071+00	291	کارشناسی ارشد	فیزیک گرایش فیزیک آماری و سامانه های پیچیده
d6127071-b534-4156-95a4-9087dcde7d6e	2026-05-13 17:45:41.018321+00	292	کارشناسی ارشد	فیزیک گرایش گرانش و کیهان شناسی
c2e3fb2b-e949-4d33-9347-4d53cb3a0e64	2026-05-13 17:52:01.717558+00	297	کارشناسی ارشد	اقیانوس شناسی فیزیکی
b35c05a6-8fa8-4d73-a628-0ce657824448	2026-05-13 17:53:33.144291+00	298	کارشناسی ارشد	فیزیک گرایش گرانش و ریاضی فیزیک
29a9e33b-7906-4732-9f3e-aa14068ae042	2026-05-13 19:29:04.704205+00	349	کارشناسی ارشد	مهندسی عمران گرایش مهندسی محیط زیست
42640f7d-7e20-446f-9556-7d0512ee9d32	2026-05-13 21:50:45.330093+00	438	دکتری	علم اطلاعات و دانش شناسی گرایش بازیابی اطلاعات و دانش
80423646-490d-4972-8be7-666f5e1bbef6	2026-05-13 21:53:53.522118+00	439	دکتری	حقوق عمومی
de4a9a8b-8c4e-4c37-b879-d313104c9155	2026-05-13 21:56:08.424418+00	443	دکتری	حقوق خصوصی
04e3acbe-9bbd-45ae-b0ab-1ed9396ebaf4	2026-05-13 21:57:11.370839+00	445	دکتری	حقوق
a1416b68-82c2-43ee-b559-4254eb38c598	2026-05-14 00:22:39.628086+00	562	کارشناسی ارشد	علوم دامی گرایش ژنتیک و اصلاح دام و طیور
c8ca27ec-9acd-4b1d-89ce-fd405080d475	2026-05-14 00:27:18.378203+00	566	کارشناسی ارشد	علوم دامی گرایش زنبور عسل
0e51cbb3-16b2-451f-a656-dec34106c9aa	2026-05-14 00:27:51.359696+00	567	کارشناسی ارشد	علوم و مهندسی شيلات گرايش تكثير و پرورش آبزیان
34d017a1-6697-43ed-a38c-36b129cc8b3b	2026-05-14 00:31:07.639687+00	570	کارشناسی ارشد	علوم و مهندسی شيلات گرايش فرآوری محصولات شیلاتی
c1e537ef-66a6-4d16-984e-85f04373a1c6	2026-05-14 00:32:10.699819+00	571	کارشناسی ارشد	علوم و مهندسی شيلات گرايش بوم شناسی آبزیان
b9c9ca74-4ccf-4d40-96df-ab0eb8b20160	2026-05-14 00:38:52.410414+00	569	کارشناسی ارشد	فناوری و كارآفرينی پايدار گرايش کارآفرینی پایدار
cecbdcd6-7243-404f-9bbe-9f1368f726af	2026-05-14 23:24:49.966963+00	637	دکتری	مهندسی کامپیوتر (نرم افزار و الگوریتم)
b869d738-3f7c-4d64-9bc6-4acbebccd0bb	2026-05-14 23:25:07.200481+00	638	دکتری	مهندسی کامپیوتر (معماری)
17fa31e7-9a8e-4463-83ec-36c30eaec69e	2026-05-11 17:15:19.662784+00	105	کارشناسی ارشد	تاریخ علم گرایش نجوم در جهان اسلام
c7ccf12f-e8f1-46a7-a31a-0a019086e1d5	2026-05-12 19:08:02.647909+00	179	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد مالی اسلامی
98324066-156f-425d-831f-9d1b04a18ccd	2026-05-12 19:14:06.588839+00	185	کارشناسی ارشد	حسابداری مدیریت
b7823ac8-4a2b-46a5-9f78-fb2ba775dc12	2026-05-12 19:14:30.402126+00	186	کارشناسی ارشد	حسابرسی
f284fef9-f690-4d4c-baeb-4f501a7f96da	2026-05-12 19:16:11.425506+00	187	کارشناسی ارشد	مدیریت مالی
044f5818-aa54-416f-87b1-10d2a7e4039a	2026-05-13 17:50:18.549769+00	296	کارشناسی ارشد	فتونیک
6252216b-2640-42c4-8b86-7c527b308840	2026-05-13 17:54:23.840656+00	299	کارشناسی ارشد	مهندسی هسته ای گرایش کاربرد پرتو ها
d2669954-8942-46a2-b1c4-0b6544030b37	2026-05-13 17:55:00.239954+00	300	کارشناسی ارشد	مهندسی هسته ای گرایش گداخت هسته ای
089b4a89-6c76-463f-b52b-68bb75820a4c	2026-05-13 17:55:47.505607+00	301	کارشناسی ارشد	مهندسی هسته ای گرایش مهندسی راکتور
db579086-7fad-4bbb-817f-821d5ab8132a	2026-05-13 17:56:20.011493+00	302	کارشناسی ارشد	مهندسی هسته ای گرایش مهندسی پرتوپزشکی
9d19a3f5-da92-4534-90ea-01ef3f8bed10	2026-05-13 19:30:15.832592+00	350	کارشناسی ارشد	مهندسی عمران گرایش مهندسی آب و سازه های هیدرولیکی
4723701a-7a3f-4e9f-bcdf-3ed8de663e3b	2026-05-13 21:55:44.730426+00	442	دکتری	حقوق بین الملل عمومی
08f437ff-c54d-43c1-98bd-e3e5f8efa5c8	2026-05-13 22:00:10.317015+00	446	دکتری	حقوق نفت و گاز
f17040d1-2c45-4c27-ae51-68d34e50f14a	2026-05-13 22:10:00.761837+00	456	دکتری	غرب شناسی انتقادی
5f73d570-2d36-47b1-bb53-66537625681b	2026-05-14 00:24:05.362692+00	565	کارشناسی ارشد	علوم دامی گرایش تغذیه طیور
fd62d48e-c99a-42d6-8644-12f15629be8e	2026-05-14 00:33:23.903765+00	572	کارشناسی ارشد	علوم و مهندسی شيلات گرايش صيد و بهره برداری آبزیان
f26b5eba-b4ae-4b52-8bf2-8b1cf900a339	2026-05-14 23:25:49.729642+00	639	دکتری	مهندسی کامپیوتر (هوش مصنوعی)
1076a140-3986-4239-956c-05510a6e8b1f	2026-05-11 17:17:32.668664+00	106	کارشناسی ارشد	تاریخ علم گرایش فیزیک و فناوری در جهان اسلام
439b6116-0d35-4431-9966-292918356938	2026-05-11 17:19:57.814475+00	107	کارشناسی ارشد	مطالعات زنان گرایش زن و خانواده
1a3c2d6d-cb23-46d1-9ad7-4e911a4e0ff3	2026-05-12 19:07:34.050822+00	180	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد مالی
5161effb-3fa5-4c80-9bb2-2ae58453173f	2026-05-12 19:08:37.441068+00	181	کارشناسی ارشد	علوم اقتصادی گرایش بانکداری اسلامی
3ed15812-378e-4327-9627-f9a601acc96c	2026-05-12 19:10:28.150974+00	184	کارشناسی ارشد	حسابداری
17a6b241-5f23-483e-879d-193ce7f11fbb	2026-05-13 18:39:58.774129+00	303	کارشناسی ارشد	مهندسی و علم کامپیوتر گرایش نرم افزار
5d0b621f-4844-461b-8bc4-ef7f679fb2c4	2026-05-13 18:40:17.022011+00	304	کارشناسی ارشد	مهندسی و علم کامپیوتر گرایش علم داده
2ce99350-c9ce-4717-9503-278f7a6940e2	2026-05-13 18:41:11.288098+00	305	کارشناسی ارشد	مهندسی و علم کامپیوتر گرایش معماری سیستم های کامپیوتر
f0f37af6-038e-47e2-b6dd-0f490f02edbe	2026-05-13 18:41:39.8462+00	306	کارشناسی ارشد	مهندسی و علم کامپیوتر گرایش هوش مصنوعی
2904d0ae-fbcf-471b-b911-4979738bd512	2026-05-13 19:30:40.169351+00	351	کارشناسی ارشد	مهندسی خطوط راه آهن
b4403e42-66fd-4b04-bfcb-32f12b6faee5	2026-05-13 21:56:46.789056+00	444	دکتری	فقه و حقوق خصوصی
d82240c0-280d-42ed-a32f-a003cdbd1509	2026-05-13 21:58:11.044168+00	447	دکتری	حقوق تجارت و سرمایه گذاری بین المللی
e4d493b9-8423-438b-a712-2b8972be8da3	2026-05-13 21:59:01.386068+00	448	دکتری	علوم سیاسی گرایش اندیشه های سیاسی
36a954ba-bb51-4eaa-9885-d7aebef18ad4	2026-05-13 21:59:49.833149+00	449	دکتری	علوم سیاسی گرایش مسائل ایران
bf411129-39df-46d2-9a45-f04cbcba76d9	2026-05-13 22:00:47.900996+00	450	دکتری	روابط بین الملل
28e8a68d-1ea0-4df2-be23-06c5cd8cd61e	2026-05-13 22:01:24.657809+00	451	دکتری	سیاست گذاری عمومی
c0106d9b-8417-41ad-b34e-ce5a51e6c677	2026-05-13 22:01:56.183065+00	452	دکتری	مطالعات منطقه ای
a5428646-2afe-448e-8fb9-d8e8f0603c98	2026-05-14 00:35:01.589106+00	573	کارشناسی ارشد	مهندسی صنايع چوب و فرآورده های سلولزی گرايش كامپوزيت های ليگنوسلولزی
2a081f70-7afc-4dcc-9493-0c0de072f2b7	2026-05-14 00:36:29.530346+00	574	کارشناسی ارشد	مهندسی صنايع چوب و فرآوردههای سلولزی گرايش حفاظت و اصلاح
1e077c40-0c1b-487f-818a-a956ca98d23d	2026-05-14 23:31:10.549592+00	640	دکتری	مهندسی پزشکی (بیومتریال)
ce383f42-2866-47f2-96a1-c5e2f19f108a	2026-05-11 17:20:27.676496+00	108	کارشناسی ارشد	مطالعات زنان گرایش حقوق زن در اسلام
94db9f8d-0ada-4d4b-9a18-39bfabe7b3f7	2026-05-12 19:09:15.77043+00	183	کارشناسی ارشد	علوم اقتصادی گرایش اقتصاد نظری
b41d39ad-9a6d-415f-8a04-014a108a27fe	2026-05-12 19:20:16.415639+00	193	کارشناسی ارشد	علوم سیاسی
8ec20edf-e979-455f-957d-fdede8b64fe1	2026-05-13 18:54:01.747381+00	307	کارشناسی ارشد	مهندسی کامپیوتر گرایش هوش مصنوعی و رباتیک
6412f636-b6be-4629-8887-7f9254d688de	2026-05-13 19:31:04.775024+00	352	کارشناسی ارشد	مهندسی حمل و نقل ریلی
693a08c4-99e7-491d-93e4-b6dd58820846	2026-05-13 19:35:18.689447+00	355	کارشناسی ارشد	مهندسی محیط زیست گرایش مواد زائد جامد
e5290ce3-c08f-40ab-9571-cc0057dc031e	2026-05-13 22:03:29.586493+00	453	دکتری	مطالعات سیاسی انقلاب اسلامی گرایش جامعه شناسی سیاسی جمهوری اسلامی ایران
8d159b3c-3b27-4ae3-b198-fd7a8946e278	2026-05-13 22:09:35.249956+00	455	دکتری	علوم سیاسی گرایش جامعه شناسی سیاسی
b8332700-686b-4c5a-8b82-4bba06858555	2026-05-13 22:13:51.641259+00	457	دکتری	مدیریت بازرگانی گرایش مدیریت بازاریابی
319126ae-e7f0-4ab2-b633-04c770331c09	2026-05-14 08:26:07.204694+00	575	کارشناسی ارشد	مهندسی صنايع چوب و فرآورده های سلولزی گرايش بيولوژی و آناتومی
56d3de63-60e2-440c-bf8a-0aacb9746570	2026-05-14 08:27:29.657177+00	576	کارشناسی ارشد	مهندسی صنايع چوب و فرآورده های سلولزی گرايش صنايع سلولزی
af1e7ae7-a261-44f7-bf74-3b5dc469dd6c	2026-05-14 08:29:15.711662+00	577	کارشناسی ارشد	مهندسی صنايع چوب و فرآورده های سلولزی گرايش مديريت صنايع چوب و فرآورده های سلولزی
272bb4f5-1b2a-41dd-a86b-d8df85fff9ca	2026-05-14 23:31:37.58662+00	641	دکتری	مهندسی پزشکی (بیومکانیک)
44f9c8c7-7f2f-45ed-b994-9388882f1ff9	2026-05-11 17:28:24.162518+00	109	کارشناسی ارشد	مدیریت راهبردی فرهنگ
45ce7dd8-1023-4331-a1a5-7b9f4cd7b645	2026-05-12 19:17:22.407681+00	188	کارشناسی ارشد	حقوق خصوصی
fce49266-1a57-4afb-977b-bfb676ff570c	2026-05-12 19:28:00.945944+00	199	کارشناسی ارشد	مدیریت صنعتی گرایش تولید و عملیات
1766e83a-2d27-486d-8c85-dbf1968fc664	2026-05-12 19:36:35.929135+00	200	کارشناسی ارشد	مدیریت صنعتی گرایش مدیریت زنجیره تامین
e7bb4713-f8c5-48f6-82df-ae1bd54b4f61	2026-05-13 18:44:11.625999+00	308	کارشناسی ارشد	مهندسی و علم کامپیوتر گرایش شبکه های کامپیوتری
918562e8-a040-4903-924b-fc1d2b58e115	2026-05-13 19:31:36.126054+00	353	کارشناسی ارشد	مهندسی راه آهن برقی
c7b396f3-685d-4893-97a6-141957b052bc	2026-05-13 19:34:18.415344+00	354	کارشناسی ارشد	مهندسی محیط زیست گرایش آب و فاضلاب
753d3673-ffad-4c4f-be9e-50174ad26a26	2026-05-13 19:36:23.719281+00	356	کارشناسی ارشد	مهندسی نقشه برداری گرایش سنجش از دور
f1988f7f-f008-4be4-a52a-e8c2800c98c4	2026-05-13 19:37:51.242519+00	359	کارشناسی ارشد	مهندسی نقشه برداری گرایش فتوگرامتری
b2564b0f-2ab2-44c4-9399-b101bf0bb9e0	2026-05-13 19:40:01.073719+00	360	کارشناسی ارشد	مهندسی سیستم های انرژی گرایش تکنولوژی انرژی
e29243d7-dbef-450b-a59c-a7c6de90b1b1	2026-05-13 19:40:31.90019+00	361	کارشناسی ارشد	مهندسی سیستم های انرژی گرایش سیستم های انرژی
0cd78ea4-ad64-4c1e-ae78-b10524f393ce	2026-05-13 22:05:30.989411+00	454	دکتری	مطالعات سیاسی انقلاب اسلامی گرایش اندیشه سیاسی رهبران انقلاب اسلامی
731f82c9-3ea6-4460-b412-313153f200e3	2026-05-14 08:30:52.436779+00	578	کارشناسی ارشد	علوم و مهندسی صنايع غذايی گرايش صنایع غذایی
9d89dc0c-19e1-444f-b228-14c2d32aa233	2026-05-14 23:32:47.891518+00	642	دکتری	مهندسی نقشه برداری گرایش فتوگرامتری
1e443073-7c31-42cf-987c-ea322d624bcc	2026-05-11 17:33:19.192347+00	110	کارشناسی ارشد	مطالعات فرهنگی و رسانه
baadcd0d-276a-4be8-bcea-3393270aded7	2026-05-11 17:34:01.076783+00	111	کارشناسی ارشد	فلسفه
38d96f57-9f93-41ef-854d-8550597609bc	2026-05-11 17:34:22.019143+00	112	کارشناسی ارشد	منطق
e6edd507-2a06-43be-a50e-375b85af5ccc	2026-05-11 17:35:59.509919+00	114	کارشناسی ارشد	علوم قرآن و حدیث
7d6dbbd1-d126-4725-a878-680072fb2548	2026-05-12 19:17:56.912191+00	189	کارشناسی ارشد	حقوق کیفری و جرم شناسی
277873df-f066-4fcb-a959-ec2dba1d6a0d	2026-05-12 19:18:28.082374+00	190	کارشناسی ارشد	حقوق بین الملل
c89b67db-773a-40f9-be0b-0882c1990356	2026-05-12 19:19:25.2234+00	192	کارشناسی ارشد	حقوق پزشکی
c4c2c110-40c7-45d8-8894-1572617785a7	2026-05-12 19:20:45.10813+00	194	کارشناسی ارشد	روابط بین الملل
a1822fcd-67a3-4b04-aca8-e38605d1a8c8	2026-05-12 19:23:10.733203+00	195	کارشناسی ارشد	مدیریت بازرگانی گرایش بازرگانی بین المللی
d0b79d6f-58b3-4e61-8e53-69516655bd30	2026-05-13 18:44:54.85841+00	309	کارشناسی ارشد	مهندسی و علم کامپیوتر گرایش رایانش امن
0749e03e-d8a8-4d60-9a4d-9be4d9ec3649	2026-05-13 19:36:45.971563+00	357	کارشناسی ارشد	مهندسی نقشه برداری گرایش ژئودزی
8ff8db54-d0aa-4be1-8ae5-7c3e47f50e6a	2026-05-13 22:48:53.838286+00	458	کارشناسی ارشد	علوم و مهندسی آبخیز گرایش سیلاب و رودخانه
b1d885b2-a347-48e3-b597-7eb0b6c5718b	2026-05-13 22:49:55.876634+00	461	کارشناسی ارشد	علوم و مهندسی آبخیز گرایش آبخیزداری شهری
3ba590fd-ecbd-4f20-acc9-56fdeac1db7f	2026-05-13 22:50:18.05187+00	460	کارشناسی ارشد	علوم و مهندسی آبخیز گرایش حفاظت آب و خاک
9386f7eb-fc17-48bb-9d0a-4520826c9a5c	2026-05-13 22:50:37.431437+00	459	کارشناسی ارشد	علوم و مهندسی آبخیز گرایش مدیریت حوزه های آبخیز
8c98b75c-81f0-43fe-8f8e-f7a1a993eb9f	2026-05-14 08:32:17.992593+00	579	کارشناسی ارشد	علوم و مهندسی صنايع غذايی گرايش شیمی مواد غذایی
e1269dc3-eb06-4472-bcd4-a5c788249c8f	2026-05-14 08:33:24.082348+00	580	کارشناسی ارشد	علوم و مهندسی صنايع غذايی گرايش زیست فناوری مواد غذایی
6c38b2a7-0581-4d1d-8341-ca3b1e9c7e1e	2026-05-14 08:34:18.092365+00	581	کارشناسی ارشد	علوم و مهندسی صنايع غذايی گرايش فناوری مواد غذایی
50676f0e-ffd8-4c9b-81c2-982b3014b043	2026-05-14 23:34:22.202688+00	643	دکتری	مهندسی مکانیک گرایش طراحی کاربردی (دینامیک , کنترل و ارتعاشات)
4ac27278-9aad-4c93-abd4-0b277aa142f9	2026-05-11 17:34:45.683881+00	113	کارشناسی ارشد	فلسفه علم
e704af11-a85d-4134-b280-4e1dd554596e	2026-05-11 17:44:25.003016+00	117	کارشناسی ارشد	فلسفه و کلام اسلامی
63c0a049-ccec-47c1-9c32-2c1229d9b1ed	2026-05-12 19:19:01.992285+00	191	کارشناسی ارشد	حقوق عمومی
b74f186d-ccf7-4478-b640-9b86a1924d20	2026-05-12 19:24:02.665284+00	195	کارشناسی ارشد	مدیریت بازرگانی گرایش بازاریابی
8bd3e63e-cd40-4d42-81d2-38f7b8bbb5dd	2026-05-12 19:24:33.912054+00	197	کارشناسی ارشد	مدیریت دولتی گرایش مدیریت تحول
d295766a-092c-4670-a141-1cd88f2929e1	2026-05-12 19:26:26.738088+00	198	کارشناسی ارشد	مدیریت صنعتی گرایش تحقیق در عملیات
7190e7cf-f842-4c8c-ba2e-18bd932d2176	2026-05-12 19:29:26.675848+00	201	کارشناسی ارشد	کارآفرینی گرایش کسب و کار جدید
53d2b531-a5d2-44c0-9282-a05d9fb70973	2026-05-13 19:37:27.224778+00	358	کارشناسی ارشد	مهندسی نقشه برداری گرایش سیستم اطلاعات مکانی
321a0ff8-72de-485a-9fdc-6c7a0b8721f6	2026-05-13 22:49:35.497167+00	462	کارشناسی ارشد	علوم و مهندسی مرتع گرایش مدیریت مرتع
1bbae511-4034-4f8a-bd3a-4bbac790a487	2026-05-13 22:56:38.666176+00	468	کارشناسی ارشد	علوم و مهندسی آب گرایش منابع آب
20025cde-20e0-4abb-ab11-32fc6c3e81ec	2026-05-14 08:35:34.85858+00	582	کارشناسی ارشد	حشره شناسی كشاورزی
68b219d3-7441-4ffe-b53e-fd72d8edf791	2026-05-14 08:38:04.639306+00	584	کارشناسی ارشد	علوم و مهندسی محيط زيست گرايش مديريت و حفاظت تنوع زيستی
84cb2dab-d7d0-483c-9699-7f705354bfd6	2026-05-15 00:32:40.19775+00	646	کارشناسی ارشد	مهندسی مكانيزاسيون كشاورزی گرايش مديريت و تحليل سامانه ها
558fd18a-2990-4baa-be87-afa7d81a917e	2026-05-11 17:42:54.292263+00	115	کارشناسی ارشد	علوم و فنون قرائات
d77fdb87-fd57-4d15-963e-9dd50038cd2d	2026-05-12 19:27:31.892472+00	32	کارشناسی	مدیریت بازرگانی
4a4a6c07-fcd5-48d4-bdd3-0420abf7e30c	2026-05-12 19:32:42.46097+00	203	کارشناسی ارشد	مدیریت فناوری گرایش همکاری ها و انتقال فناوری
04ac0f65-6c9d-4c9e-9ef2-9fe784d1b6d3	2026-05-12 19:33:47.8998+00	204	کارشناسی ارشد	مدیریت فناوری گرایش نوآوری
6be414ff-a691-4636-8e6a-c93b2150491d	2026-05-13 19:55:25.727006+00	362	دکتری	تاریخ گرایش تاریخ ایران بعد از اسلام
38c7808d-41c9-4f63-83d9-5b959d019366	2026-05-13 22:49:16.767781+00	463	کارشناسی ارشد	علوم و مهندسی مرتع گرایش اصلاح و احیای مرتع
10deb661-7408-46ff-aa1e-5835cf6d0fb8	2026-05-14 08:36:43.826434+00	583	کارشناسی ارشد	بيماری شناسی گياهی
14973e72-412a-4a6d-ae1d-0cae1142e010	2026-05-15 00:31:40.159259+00	644	کارشناسی ارشد	مهندسی مكانيزاسيون كشاورزی گرايش انرژی
5f877a47-35cd-4fad-b723-f560b767bf27	2026-05-15 00:32:11.989619+00	645	کارشناسی ارشد	مهندسی مكانيزاسيون كشاورزی گرايش مدیریت پسماند
b4aa8c51-5c44-4f32-b262-60c17c3f3b23	2026-05-15 00:34:03.447654+00	647	کارشناسی ارشد	اكوهيدرولوژی
6401102e-9222-4c84-9cfe-c11954a14ae7	2026-05-11 17:43:15.690717+00	116	کارشناسی ارشد	تفسیر و علوم قرآن
580381a1-4858-4f9d-a677-55df37691f7a	2026-05-12 19:31:03.779421+00	202	کارشناسی ارشد	کارآفرینی گرایش کسب و کار الکترونیکی
122efe43-cad8-4eaa-a699-2c61776dbfb5	2026-05-13 20:04:12.47334+00	363	دکتری	تاریخ گرایش تاریخ اسلام
1a6acf6b-3703-45ca-9154-89a240fa1c01	2026-05-13 20:10:54.36708+00	366	دکتری	تاریخ تشیع اثنی عشری
ba267f22-0b00-45c5-8b09-8404ac2aecb7	2026-05-13 22:51:49.184121+00	464	کارشناسی ارشد	علوم و مهندسی مرتع گرایش گیاهان دارویی و صنعتی
796292d0-8162-487e-96aa-c1bf9c4708a0	2026-05-13 22:54:57.068631+00	467	کارشناسی ارشد	علوم و مهندسی آب گرایش رودخانه و اکوسیستم های آبی
ad697624-e318-480d-970d-1843ba566257	2026-05-14 09:17:14.883213+00	585	کارشناسی ارشد	علوم و مهندسی محيط زيست گرايش ارزيابی و آمايش سرزمين
e04ccf65-f216-4992-a201-5b45f9109f58	2026-05-14 09:21:25.92614+00	586	کارشناسی ارشد	علوم و مهندسی محيط زيست گرايش آلودگی محيط زيست
4e222a61-39ba-44e4-8acd-9020872740ac	2026-05-14 09:23:56.778577+00	587	کارشناسی ارشد	فناوری و كارآفرينی پايدار گرايش كارآفرينی پايدار
3cd23a6f-5950-499c-997c-4227eff39661	2026-05-15 00:34:44.544502+00	648	کارشناسی ارشد	بيوتكنولوژی كشاورزی
89b37b01-443b-45c7-b96a-84c80503df71	2026-05-15 00:35:31.162789+00	649	کارشناسی ارشد	توسعه روستایی
24e42f8b-4e50-4aa9-bb70-e10a2caeabc5	2026-05-15 00:38:45.026901+00	653	کارشناسی ارشد	برنامه ريزی منطقه ای
d69f44e7-7b56-4613-ba87-3c1f648cdb87	2026-05-15 00:41:15.009774+00	655	کارشناسی ارشد	محيط زيست شهری
c9af7a81-2ee3-4a3d-867f-b3fae152b3b0	2026-05-11 19:51:37.959104+00	118	کارشناسی ارشد	مدرسی مبانی نظری اسلام
f5c85bdd-23d8-4b68-9468-bdcb6dfab5e2	2026-05-11 19:53:12.293071+00	119	کارشناسی ارشد	مدرسی معارف قرآن و حدیث
b00b49c9-a2a3-4129-82f2-54dced9aa49d	2026-05-12 19:36:06.937573+00	205	کارشناسی ارشد	مالی گرایش مهندسی مالی و مدیریت ریسک
480b7012-84b7-4afb-bb83-df46adcc31d5	2026-05-13 20:04:34.163271+00	364	دکتری	تاریخ گرایش تاریخ محلی
b1f05ddc-2eae-4dfc-b4c8-fd12bfc8c00c	2026-05-13 22:52:21.328274+00	465	کارشناسی ارشد	علوم و مهندسی آب گرایش آبیاری و زهکشی
c4e38619-087d-49d4-9a2e-3c1eca8fee65	2026-05-13 22:52:54.638131+00	466	کارشناسی ارشد	علوم و مهندسی آب گرایش سازه های آبی
59860a54-2577-4485-8e0a-b345f7b2b114	2026-05-14 09:25:46.889356+00	588	کارشناسی ارشد	مديريت يكپارچه منطقه ساحلی گرايش مديريت ناحيه ساحلی
14503f8d-908f-4658-9e0a-15f603188660	2026-05-14 09:27:47.281549+00	589	کارشناسی ارشد	مديريت يكپارچه منطقه ساحلی گرايش مديريت ناحيه كرانه ای
cf4fe1d8-48fb-4349-8045-170e0821095d	2026-05-14 09:29:49.790433+00	591	کارشناسی ارشد	مهندسی گلخانه
7c328b50-3621-40a6-b7cc-d380e4a7c9e3	2026-05-15 00:36:07.664656+00	650	کارشناسی ارشد	مديريت كشاورزی
97c25119-87c6-4cde-91da-d42a8583a943	2026-05-15 00:36:51.45534+00	651	کارشناسی ارشد	مهندسی فضای سبز
620c6bcb-f161-422a-9f0b-878fe5bdf726	2026-05-11 20:00:23.608103+00	120	کارشناسی ارشد	مطالعات قرآن و حدیث گرایش معارف قرآن
9035e936-8c75-4dd7-ac08-34a4c7173c13	2026-05-12 19:39:21.735266+00	206	کارشناسی ارشد	مدیریت منابع انسانی گرایش مدیریت عملکرد و بهره‌وری منابع انسانی
5fbbd38a-7e67-4293-a7c1-c59905a58876	2026-05-12 19:40:13.748296+00	207	کارشناسی ارشد	مدیریت منابع انسانی گرایش مدیریت منابع انسانی اسلامی
515486ff-5312-4ca2-88f6-4ce90206b328	2026-05-12 19:42:54.295249+00	208	کارشناسی ارشد	مدیریت کسب و کار گرایش استراتژی
ec47c959-ca04-4f3a-b245-2cb6ee0d6fee	2026-05-12 19:43:10.162554+00	209	کارشناسی ارشد	مدیریت کسب و کار گرایش مالی
e4f858a7-fb5f-44cd-90f4-05fb7a35d3ed	2026-05-13 20:05:06.450364+00	365	دکتری	تاریخ انقلاب اسلامی
9fd3ab37-e98a-4897-8c3b-2ea0cfe73576	2026-05-13 23:07:31.597138+00	469	کارشناسی ارشد	علوم و مهندسی آب گرایش مدیریت و برنامه ریزی منابع آب
706f831d-c3c3-417a-8a1d-f1b3839aa566	2026-05-13 23:08:15.401503+00	470	کارشناسی ارشد	مهندسی طبیعت
1c7fcd29-a07e-49a9-8c5d-0640290c814c	2026-05-13 23:08:50.943608+00	471	کارشناسی ارشد	حکمرانی آب
e762a785-cde0-4a29-a080-56401d8d3e4a	2026-05-14 09:28:49.448368+00	590	کارشناسی ارشد	مهندسی مكانيک بيوسيستم گرايش طراحی و ساخت
40a00070-490d-46eb-a868-fc583a57bc40	2026-05-15 00:38:07.02147+00	652	کارشناسی ارشد	برنامه ريزی شهری
d29e6254-2838-433b-aa26-3858599d4d52	2026-05-15 00:40:41.020931+00	654	کارشناسی ارشد	مديريت شهری
7c837335-586b-4fac-9a29-6a2c4f7a70de	2026-05-15 00:46:17.515361+00	659	کارشناسی ارشد	مهندسی معماری گرايش معماری آموزشی و فرهنگی
a6b2d556-fd86-4af2-b057-b5a030d85bf1	2026-05-15 00:47:25.643106+00	660	کارشناسی ارشد	مهندسی معماری گرايش فضاهای درمانی و بهداشتی
5a2c260d-0e96-4279-8d1c-6da4bbe4c06b	2026-05-15 00:48:21.688007+00	661	کارشناسی ارشد	مهندسی معماری گرايش مسكن
2df56e04-e967-4717-8fbe-7886206af223	2026-05-11 20:02:19.470369+00	121	کارشناسی ارشد	علوم حدیث گرایش تفسیر اثری
74fa4e90-e18c-4599-b726-1a5fae4cbf19	2026-05-11 20:03:24.411306+00	122	کارشناسی ارشد	علوم حدیث گرایش اقتصاد اسلامی
2264bae3-f6d1-483b-8ae4-7d12e8b3a29a	2026-05-12 19:43:37.500291+00	210	کارشناسی ارشد	مدیریت کسب و کار گرایش بازاریابی
1df11d8b-53b2-4e86-9a7b-c8f7a102e09c	2026-05-12 19:44:38.641181+00	211	کارشناسی ارشد	مدیریت کسب و کار گرایش عملیات و زنجیره تامین
e0f47b77-0180-4061-bd12-4bbffb715a33	2026-05-12 19:45:18.526462+00	212	کارشناسی ارشد	مدیریت کسب و کار گرایش رفتار سازمانی و منابع انسانی
d78917f7-9491-4d8b-b7d5-d4987cf9bbad	2026-05-12 19:47:24.869179+00	215	کارشناسی ارشد	روان شناسی عمومی
b21b898a-39b7-4514-8e76-699dad0de290	2026-05-12 19:47:54.08901+00	216	کارشناسی ارشد	علوم شناختی گرایش روان شناسی شناختی
076d3422-9bfd-4d9e-b4e1-741dd08f8a7b	2026-05-13 20:11:46.342181+00	367	دکتری	تاریخ گرایش تاریخ ایران قبل از اسلام
180de767-86d8-4bbb-bce8-e0c72fe2ca25	2026-05-13 20:13:00.196858+00	369	دکتری	زبان و ادبیات فارسی گرایش ادبیات حماسی
737766fb-f6a3-4375-8b8e-a9676d3c2ef2	2026-05-13 23:09:22.22341+00	472	کارشناسی ارشد	علوم و مهندسی آب گرایش هواشناسی کشاورزی
a3bbb505-ed8a-40a7-9546-bfeaf7526217	2026-05-13 23:18:34.175278+00	483	کارشناسی ارشد	حکمرانی کشاورزی و منابع طبیعی
5272ae6a-f056-4890-9524-ea9091376ac7	2026-05-14 09:31:15.399325+00	592	کارشناسی ارشد	مهندسی مكانيک بيوسيستم گرايش انرژی های تجديدپذير
45e5f4d1-ef8e-4040-adc5-e2605297ca2f	2026-05-15 00:42:52.883834+00	656	کارشناسی ارشد	طراحی شهری
c98e7c13-e1c0-487b-9fb2-9bd63d52232b	2026-05-15 00:43:51.635685+00	657	کارشناسی ارشد	معماری محيط های يادگيری
d281ab55-8020-4660-a056-8d7736a36b86	2026-05-15 00:52:02.966831+00	666	کارشناسی ارشد	مهندسی معماری اسلامی
bbe840ab-a4ec-4822-927c-05a25f49468c	2026-05-11 20:08:32.619563+00	123	کارشناسی ارشد	نهج البلاغه گرایش اخلاق و تربیت فردی و اجتماعی
97e077c4-b9ff-4e1c-a4f1-c1e869e50f61	2026-05-12 19:45:57.278733+00	213	کارشناسی ارشد	روان شناسی بالینی
cf73fb02-8fc8-44a5-8899-914d44a9b91b	2026-05-13 20:12:22.692672+00	368	دکتری	زبان و ادبیات فارسی
b2307259-82a7-4b7e-9d3a-8f10e5f2a5ff	2026-05-13 20:13:36.596078+00	370	دکتری	زبان و ادبیات فارسی گرایش ادبیات غنایی
f7088dcf-d825-4500-83bf-eab1a21c712d	2026-05-13 23:10:04.612207+00	473	کارشناسی ارشد	علوم و مهندسی آب گرایش هیدروانفورماتیک
2db53e2d-33d0-4685-a486-7b3995f6265f	2026-05-13 23:11:03.650984+00	474	کارشناسی ارشد	ژنتیک و به نژادی گیاهی
d7e95982-79d7-44b6-b62d-c13df5ca79ef	2026-05-14 09:42:06.984436+00	593	کارشناسی ارشد	مهندسی مكانيک بيوسيستم گرايش فناوری پس از برداشت
04ae49fe-ca45-4721-9e13-2b139b1bda93	2026-05-15 00:44:28.471897+00	658	کارشناسی ارشد	مهندسی معماری
c48a5f27-e95e-46c3-84e7-7a601066af15	2026-05-11 20:21:34.036014+00	124	کارشناسی ارشد	منطق فهم دین
da7f002b-b973-48da-86f2-5c4ab2bb83bd	2026-05-12 19:47:07.704884+00	214	کارشناسی ارشد	روان شناسی صنعتی و سازمانی
31716a39-95da-4b34-b6af-cf7820085fa3	2026-05-12 19:48:33.244794+00	217	کارشناسی ارشد	روان شناسی بالینی کودک و نوجوان
e6c264cb-ded2-44e4-a1a7-fdd15e9cccca	2026-05-12 19:49:06.002543+00	218	کارشناسی ارشد	روان شناسی و آموزش کودکان استثنایی
fc40557e-98d6-41d7-b884-19a9c9f5787a	2026-05-12 19:50:50.019454+00	219	کارشناسی ارشد	علم اطلاعات و دانش شناسی گرایش مدیریت اطلاعات
6a3d33d3-aefa-4cce-b67b-a05e0521eb92	2026-05-13 20:13:59.996351+00	371	دکتری	زبان و ادبیات فارسی گرایش ادبیات عرفانی
46997e6c-c11d-4a6c-857e-b75a81d7cd47	2026-05-13 20:14:39.570101+00	372	دکتری	زبان و ادبیات فارسی گرایش آموزش زبان فارسی
dc0d11ff-71ea-48f4-86cf-0f1330172480	2026-05-13 20:15:54.913965+00	373	دکتری	آمورش زبان و ادبیات فارسی به غیر فارسی زبانان
2031a3a4-3dfb-450a-8f46-191bba701932	2026-05-13 20:17:08.749763+00	374	دکتری	جامعه شناسی گرایش جامعه شناسی اقتصادی و توسعه
cc4ae335-1b71-4830-8d3a-44ff316e6bfe	2026-05-13 20:18:20.697822+00	377	دکتری	جامعه شناسی گرایش جامعه شناسی سیاسی
9f752aeb-eabb-42d2-a4db-1ed1185cb5f0	2026-05-13 20:19:43.209579+00	379	دکتری	فلسفه محض
c3278d95-bc1b-42c6-a4a5-359d398f0a59	2026-05-13 23:11:37.779344+00	475	کارشناسی ارشد	اگرو تكنولوژی گرايش فیزيولوژی گياهان زراعی
88d6ca25-d550-47a3-884a-d7b7922bd4d6	2026-05-14 09:43:13.191817+00	594	کارشناسی ارشد	مديريت و كنترل بيابان
f76c7b00-06e3-4360-ba83-b81d6cab980b	2026-05-15 00:49:03.11626+00	662	کارشناسی ارشد	مهندسی معماری گرايش پايداری
94f04b06-a8be-4da6-8a60-668f07395ace	2026-05-15 00:49:56.128325+00	663	کارشناسی ارشد	مهندسی معماری گرايش مهندسی فناوری
ba4e2fb9-3b11-4a81-9dab-193758ca43a0	2026-05-15 00:50:37.745505+00	664	کارشناسی ارشد	مديريت آموزشگاهی ويژه هنرستان ها
449b7b39-e7ea-473d-8c33-e045bf16cba0	2026-05-11 20:26:54.892541+00	125	کارشناسی ارشد	امام شناسی و معارف ائمه (ع)
e610ba1a-a539-4681-a73e-2f891f4ff5e7	2026-05-12 19:51:32.02931+00	220	کارشناسی ارشد	علم اطلاعات و دانش شناسی گرایش مدیریت کتابخانه های دانشگاهی
f75c37d1-cdce-4b65-8e64-505c36270ee9	2026-05-12 19:56:29.173774+00	225	کارشناسی ارشد	برنامه ریزی آموزشی
2fb30275-2682-41ef-a281-c5471dbc8238	2026-05-12 19:57:08.180457+00	226	کارشناسی ارشد	تکنولوژی آموزشی گرایش آموزش افراد با نیاز های ویژه
a989619d-0443-4173-bd16-b4bcfeb54ec7	2026-05-12 19:57:29.739482+00	227	کارشناسی ارشد	برنامه ریزی درسی
fe70f0a9-3345-4bb0-a98c-4201e051395c	2026-05-12 19:59:31.837929+00	230	کارشناسی ارشد	مشاوره گرایش مشاوره مدرسه
a264a3f8-eea6-43f2-9cd8-7c7eafabe89a	2026-05-13 20:17:47.468866+00	375	دکتری	جامعه شناسی گرایش جامعه شناسی مسائل اجتماعی ایران
6534d822-add2-40c6-a771-fd45a920401d	2026-05-13 20:27:43.497391+00	380	دکتری	فلسفه دین
d835dcc9-a0ea-423c-a331-79db72e77f73	2026-05-13 20:28:57.116002+00	383	دکتری	فلسفه تعلیم و تربیت
bc7ed73a-30fc-408b-9f1a-34b65d5821d5	2026-05-13 23:12:52.237488+00	476	کارشناسی ارشد	اگرو تکنولوژی گرایش علوم و تکنولوژی بذر
2c2650ef-08cf-41a4-9731-60ac594f45db	2026-05-13 23:13:40.258319+00	477	کارشناسی ارشد	اگرو تکنولوژی گرایش علوم علف های هرز
cb659993-e15f-4592-817e-72195e0d2beb	2026-05-13 23:14:41.023708+00	478	کارشناسی ارشد	اگرو تکنولوژی
b25c1281-0c32-433a-9c0f-9c767638fa70	2026-05-13 23:15:35.397866+00	479	کارشناسی ارشد	اقتصاد کشاورزی گرایش سیاست و توسعه کشاورزی
c852c38c-5fc2-49f5-9702-b32351747562	2026-05-13 23:16:20.456937+00	480	کارشناسی ارشد	اقتصاد کشاورزی گرایش اقتصاد تولید و مدیریت واحد های کشاورزی
10baab80-95f5-4cc9-8791-f819c2555816	2026-05-13 23:17:52.790821+00	482	کارشناسی ارشد	اقتصاد کشاورزی گرایش بازاریابی محصولات کشاورزی
267c574f-f7af-47cf-9137-d1a355a2a49a	2026-05-14 14:04:04.458566+00	595	دکتری	علوم شناختی گرایش روانشناسی شناختی
b8f5522b-07d8-4bb0-8c6d-ab446152c213	2026-05-14 14:05:42.608119+00	597	دکتری	مهندسی برق گرایش مخابرات سیستم
eb824c4b-595b-471b-87ee-0c81e73c9c38	2026-05-15 00:51:06.664055+00	665	کارشناسی ارشد	آموزش مهندسی
817147fc-29cb-46ae-80e0-a3ccddfbcd26	2026-05-11 20:30:04.441756+00	126	کارشناسی ارشد	فلسفه دین
fae440e8-f351-40aa-a00f-cd9cc713a85e	2026-05-12 19:53:03.37668+00	221	کارشناسی ارشد	علم اطلاعات و دانش شناسی گرایش مدیریت کتابخانه های دیجیتال
e308523f-87a4-4586-8074-da4494a7d72f	2026-05-12 19:54:03.426252+00	222	کارشناسی ارشد	مدیریت آموزشی
3722b2a6-7d2c-492d-99e6-182535dc3005	2026-05-12 19:55:10.765035+00	223	کارشناسی ارشد	تاریخ و فلسفه آموزش و پرورش
2ef95315-fb5a-450f-876f-b935d0922a9a	2026-05-12 19:55:58.598958+00	224	کارشناسی ارشد	تکنولوژی آموزشی
33811450-d529-47e0-b1a3-c25517304c3f	2026-05-12 19:59:15.788133+00	229	کارشناسی ارشد	مشاوره گرایش مشاوره خانواده
8856b68d-18cc-4e33-9f41-6e18b7c5fdb5	2026-05-12 20:00:10.046553+00	231	کارشناسی ارشد	مشاوره گرایش مشاوره توانبخشی
af23c59e-333e-4196-af2d-dda074079fad	2026-05-13 20:18:03.576723+00	376	دکتری	جامعه شناسی گرایش جامعه شناسی فرهنگی
059102f9-60d9-4666-bb26-e74b0e3c900c	2026-05-13 20:19:15.281855+00	378	دکتری	جامعه شناسی گرایش توسعه اجتماعی - روستایی
781aefc8-cc26-4c97-8389-0ded96015061	2026-05-13 23:17:08.75799+00	481	کارشناسی ارشد	اقتصاد کشاورزی گرایش اقتصاد منابع طبیعی و محیط زیست
2a351d1d-c460-4bcc-9d14-6a5051086a8c	2026-05-13 23:24:23.625594+00	485	دکتری	مدیریت بازرگانی گرایش مدیریت سیاست گذاری بازرگانی
c659ecd0-3acc-47fe-bd15-71eec4d8dabd	2026-05-13 23:25:17.838703+00	486	دکتری	مدیریت بازرگانی گرایش رفتار سازمانی و مدیریت منابع انسانی
74e09b9c-b1b0-48a5-953d-bce26160f087	2026-05-14 14:04:36.612556+00	596	دکتری	مهندسی برق گرایش الکترونیک
75497faf-4269-4141-956a-b608a4f9d824	2026-05-15 15:05:30.445852+00	667	کارشناسی ارشد	مطالعات معماری ايران
b8afcb18-8f32-44b0-a1ab-971a6aa8f3c5	2026-05-15 15:06:57.557286+00	668	کارشناسی ارشد	بازسازی پس از سانحه
72bb9b53-846f-49dc-bce6-10861d9c7ed3	2026-05-15 15:09:39.028753+00	670	کارشناسی ارشد	مرمت و احيا ابنيه و بافت های تاريخی
a8b62271-5b1a-45a2-ba3e-22020d9e24c2	2026-05-15 15:10:55.732189+00	671	کارشناسی ارشد	مرمت و احيا ابنيه و بافت های تاريخی گرايش حفاظت و مرمت ميراث معماری
d5cf8343-0328-4662-8971-41aa22a3d073	2026-05-11 20:38:39.586881+00	127	کارشناسی ارشد	آمار ریاضی
75646137-0005-44ed-93d0-b1f0d17ca11a	2026-05-12 19:58:36.463834+00	228	کارشناسی ارشد	مشاوره گرایش مشاوره شغلی
0c5cc3b8-3669-4a1f-b24d-8b605a22983a	2026-05-13 23:19:35.683009+00	484	کارشناسی ارشد	علوم و مهندسی باغبانی گرایش درختان میوه
561cdb92-f935-4f95-984c-33ae9accce2c	2026-05-14 14:06:29.2759+00	598	دکتری	مهندسی برق گرایش قدرت
14b0907c-21f1-4825-b2fc-e131ba5fc815	2026-05-15 15:08:26.380405+00	669	کارشناسی ارشد	معماری داخلی
b1b204f2-874f-4f6d-9f81-c4707db5a16b	2026-05-15 15:17:19.304531+00	673	کارشناسی ارشد	مرمت اشيا فرهنگی و تاريخی
433dfbd4-0290-4f10-9d2c-20097d0dafdf	2026-05-11 20:40:07.701594+00	128	کارشناسی ارشد	آمار اقتصادی
ca25090d-6619-4396-afd5-e8d335871ab2	2026-05-13 06:20:56.92491+00	232	کارشناسی ارشد	جغرافیا و برنامه ریزی روستایی گرایش توسعه اقتصاد روستایی
c4201030-e801-4bac-9d44-abcd297c040a	2026-05-13 20:28:16.857209+00	381	دکتری	فلسفه هنر
2f8899ad-7f23-43ca-8d35-22477e2a22ca	2026-05-13 20:28:38.241049+00	382	دکتری	فلسفه اخلاق
b9d9a9ca-b4ca-4b78-8fdc-58f70ab1571a	2026-05-13 23:26:42.57743+00	488	دکتری	مدیریت دولتی گرایش تصمیم گیری و خط مشیگذاری عمومی
090c8257-59ba-4446-bed2-cd8517999426	2026-05-13 23:29:43.756378+00	487	دکتری	مدیریت راهبردی
eeb6444e-fab8-4ec3-818d-852c90024091	2026-05-14 14:07:15.347866+00	599	دکتری	مهندسی برق گرایش کنترل
d064bb3e-874b-42cc-8133-864621ccf96b	2026-05-14 14:07:54.208982+00	600	دکتری	مهندسی عمران گرایش سازه
f2085eef-6a82-4d88-93a5-db21e65dfaa5	2026-05-15 15:12:27.657105+00	672	کارشناسی ارشد	مرمت و احيا ابنيه و بافت های تاريخی گرايش حفاظت و مرمت ميراث شهری
836c1cd8-ad1c-4c22-a244-71c7b63cc4df	2026-05-11 20:40:29.175203+00	129	کارشناسی ارشد	آمار اجتماعی
0f69ecd6-a26f-4fc1-abef-e0f1255596ed	2026-05-11 20:44:02.88935+00	130	کارشناسی ارشد	ریاضیات و کاربرد ها گرایش آنالیز
b8b7dcaf-a8d2-465a-af7b-958c1dcd8c07	2026-05-11 20:44:36.54788+00	131	کارشناسی ارشد	ریاضیات و کاربرد ها گرایش جبر
b174086f-1d8d-4aed-8f3b-d6a47830c733	2026-05-11 20:47:47.830026+00	132	کارشناسی ارشد	ریاضیات و کاربرد ها گرایش هندسه و توپولوژی
e5f55f13-64e9-49f4-9308-1e40e24de48d	2026-05-13 06:22:06.994566+00	233	کارشناسی ارشد	جغرافیا و برنامه ریزی روستایی گرایش برنامه ریزی کالبدی - فضایی
7d4f1f5b-2653-4587-a716-cacf2f5b9986	2026-05-13 20:31:49.251596+00	384	دکتری	جغرافیای سیاسی
f6d8a5af-1e5f-4bd6-ae74-148acb05832d	2026-05-13 20:32:12.549787+00	385	دکتری	جغرافیا و برنامه ریزی شهری
d0bcb5c4-6231-4a26-ac10-acc8ed003013	2026-05-13 20:32:51.4238+00	386	دکتری	برنامه ریزی آمایش سرزمین
c0c8a977-eb8d-4d9a-b975-8aeb8026ff6d	2026-05-13 20:33:13.140558+00	387	دکتری	جغرافیا و برنامه ریزی روستایی
87e9831a-7831-4b87-8189-9ea37f261008	2026-05-13 20:34:01.345747+00	389	دکتری	آب و هوا شناسی
72afdb4c-14ef-405a-b59f-cd5e595fe9a4	2026-05-13 20:35:01.456635+00	390	دکتری	سنجش از دور و سامانه اطلاعات جفرافیایی
48c3f847-894c-489e-aa07-f1e89deffeda	2026-05-13 23:27:09.905644+00	489	دکتری	مدیریت دولتی گرایش رفتار سازمانی
1bace572-a645-40df-acc2-fe8f76e0e07e	2026-05-13 23:27:59.01304+00	490	دکتری	مدیریت دولتی گرایش مدیریت منابع انسانی
07e8440a-3785-491c-8ac5-d3c6a8c98809	2026-05-14 14:09:38.062419+00	601	دکتری	مهندسی عمران گرایش مهندسی و مدیریت منابع آب
78651950-bbda-423e-ad6b-6e74cb33b0ac	2026-05-14 14:10:26.044191+00	602	دکتری	مهندسی نقشه برداری گرایش ژئودزی
5e4856b5-6aee-4cee-9f81-575141668134	2026-05-14 14:14:51.675905+00	607	دکتری	مهندسی مکانیک گرایش تبدیل انرژی
671d3584-8a16-4c64-88a1-1d71b20f75fd	2026-05-15 15:22:08.197351+00	674	کارشناسی ارشد	مديريت ميراث فرهنگی گرايش ابنيه و اماكن تاريخی - فرهنگی
6c55244c-441b-4bf5-b780-b21b0923110e	2026-05-15 15:28:57.226159+00	678	کارشناسی ارشد	كارگردانی نمايش
09ca999d-ffd2-4c7d-97f9-771a4786bef0	2026-05-11 20:53:32.46315+00	133	کارشناسی ارشد	ریاضی کاربردی گرایش بهینه سازی
d851517d-1cbd-4359-82a6-6036df1b77da	2026-05-13 06:23:36.47576+00	234	کارشناسی ارشد	جغرافیا و برنامه ریزی روستایی گرایش مدریت توسعه پایدار روستایی
1df26835-19c9-4cdd-8e92-38c072d817da	2026-05-13 06:32:24.693904+00	238	کارشناسی ارشد	جغرافیا و برنامه ریزی شهری گرایش برنامه ریزی مسکن و بازآفرینی شهری
ca79849f-fe4e-43e3-8209-eda4e2e48ab7	2026-05-13 20:33:33.653591+00	388	دکتری	ژئومورفولوژی
71377104-858c-4b2a-a2f8-89a9387676a8	2026-05-13 20:37:53.456832+00	394	دکتری	علوم اقتصادی گرایش اقتصاد بخش عمومی
6cfc57bf-c372-4adb-a602-7369186352f8	2026-05-13 20:41:12.532319+00	401	دکتری	علوم اقتصادی گرایش اقتصاد پولی
a79b845c-15a7-4e25-9f37-44183bac0025	2026-05-13 23:28:33.501835+00	491	دکتری	مطالعات مدیریت اسلامی
11ee1967-b029-4006-bd5f-b57b1c6461b0	2026-05-13 23:29:18.176601+00	492	دکتری	مدیریت صنعتی گرایش تولید و عملیات
e3623eab-1065-4d33-9429-8bd4f3d5ca62	2026-05-13 23:32:37.961531+00	497	دکتری	مدیریت فناوری اطلاعات گرایش کسب و کار هوشمند
d24a4e54-2630-4300-9c6f-df77406840a1	2026-05-13 23:33:22.428594+00	498	دکتری	کارآفرینی
d497b254-0318-4bac-a5bb-efadb58b7d64	2026-05-13 23:33:53.7753+00	499	دکتری	مالی گرایش مهندسی مالی
1712a1cc-eda7-4584-8608-f6b6cadff535	2026-05-13 23:34:20.740677+00	500	دکتری	مالی گرایش بانکداری
926d9d54-2773-4854-92f0-6f89250f3b78	2026-05-14 14:11:13.775644+00	603	دکتری	مهندسی نقشه برداری گرایش سنجش از دور
bf2bd070-5665-4007-b675-023e034f7042	2026-05-14 14:12:44.732582+00	605	دکتری	مهندسی مکانیک گرایش ساخت و تولید
0f9da2bc-cd9e-43be-a145-b2e55c5ca1ff	2026-05-14 14:13:36.081811+00	606	دکتری	مهندسی مکانیک گرایش طراحی کاربردی
2e565213-d80d-4a6d-96c0-9fb5e27d3f0e	2026-05-15 15:26:05.853463+00	675	کارشناسی ارشد	باستان سنجی گرايش آثار و مواد آلی
9a675754-1d87-4587-b1f3-9a6af68861dc	2026-05-15 15:26:54.709452+00	676	کارشناسی ارشد	باستان سنجی گرايش آثار و مواد معدنی
94660a9b-4407-4f56-b77c-883abfe3d570	2026-05-11 22:17:41.601815+00	134	کارشناسی ارشد	ریاضی کاربردی گرایش آنالیز عددی
6ef76bc6-bb2e-42dd-a674-a8d02e5a6e8c	2026-05-13 06:29:23.292295+00	235	کارشناسی ارشد	جغرافیای سیاسی گرایش آمایش سیاسی فضا
c399d68b-587d-4c21-bdd0-b1600919c795	2026-05-13 06:30:20.10413+00	236	کارشناسی ارشد	جغرافیا و برنامه ریزی شهری گرایش آمایش شهری
2203374e-db1f-471b-876a-067c8624429e	2026-05-13 06:31:11.151886+00	237	کارشناسی ارشد	جغرافیا و برنامه ریزی شهری گرایش محیط زیست شهری
c74f09f8-5746-41ae-b49d-78d61bec40a5	2026-05-13 20:36:11.289176+00	391	دکتری	زبان و ادبیات عربی
9bebe53a-d6aa-4f5d-a282-cb11a301203b	2026-05-13 20:37:16.763308+00	393	دکتری	علوم اقتصادی گرایش اقتصاد بین الملل
245cf769-1e28-4353-ade3-def6bd14a47b	2026-05-13 20:40:05.102984+00	399	دکتری	علوم اقتصادی گرایش توسعه اقتصادی
4cea07b3-4448-4161-869d-bb4d8c8df4e6	2026-05-13 23:30:21.201407+00	493	دکتری	مدیریت صنعتی گرایش تحقیق در عملیات
db045a38-c436-471b-8cc8-56e2b79dfe7a	2026-05-13 23:31:51.881949+00	496	دکتری	مدیریت رسانه
0e3e479d-55ad-42da-b493-c27c80416695	2026-05-14 14:11:49.47643+00	604	دکتری	مهندسی مکانیک گرایش سیستم اطلاعات مکانی
39e6ab56-817f-4349-b380-c197a5aa97a9	2026-05-15 15:27:56.443784+00	677	کارشناسی ارشد	نمايش عروسكی
6e7ecfa7-4906-470d-b8d4-a07c2fc5744a	2026-05-11 22:18:36.179483+00	135	کارشناسی ارشد	ریاضی کاربردی گرایش ریاضی مالی
b7c1a5b4-9d66-4ccc-ad0d-b38dd7cd6244	2026-05-13 06:36:25.01998+00	239	کارشناسی ارشد	جغرافیا و برنامه ریزی شهری گرایش کاربری اراضی و ممیزی املاک
200b1366-aa8e-4bec-be68-e6eb3c22880f	2026-05-13 20:36:32.090048+00	392	دکتری	مطالعات ترجمه عربی
3e17428c-f30f-4c8c-a020-49c0f0b10729	2026-05-13 20:38:25.570566+00	395	دکتری	علوم اقتصادی گرایش اقتصاد سنجی
8df607e2-31d0-4c59-94ac-f452d3f495aa	2026-05-13 20:39:49.676028+00	398	دکتری	علوم اقتصادی گرایش اقتصاد منابع
5a2a9451-cee2-43d3-8287-b8b4509aef51	2026-05-13 23:31:04.818458+00	494	دکتری	مدیریت صنعتی گرایش مدیریت سیستم ها
2eff9142-d4b1-47a7-b508-36ba679c954f	2026-05-13 23:31:30.653293+00	495	دکتری	مدیریت آماد و پشتیبانی
fdef4b2b-ef1a-4a55-bf8e-9157dc609ee4	2026-05-14 14:29:26.080869+00	614	دکتری	مهندسی شیمی گرایش بیوتکنولوژی
81dca151-4f66-43b3-a5d1-518661e22c85	2026-05-14 23:30:25.856601+00	609	دکتری	مهندسی پزشکی (بیوالکتریک)
3ca70a59-9bb4-49ab-817e-efd55cc1e9ae	2026-05-15 21:45:30.881698+00	679	دکتری	روان شناسی عمومی
db1e980e-f2d1-451d-8ae5-8df941d4e799	2026-05-11 22:22:43.233188+00	136	کارشناسی ارشد	ریاضی کاربردی گرایش رمز و کد
2ca30e8f-bce9-4ae9-a176-182beb88f04b	2026-05-11 22:23:19.086781+00	137	کارشناسی ارشد	ریاضی کاربردی گرایش علوم داده
554286ef-a64b-4d14-a6ee-d41f025aa45d	2026-05-11 22:24:13.719362+00	138	کارشناسی ارشد	محاسبات و ریاضیات مهندسی
c0050b1d-e1dc-43e4-80bd-0309db6fada9	2026-05-13 06:45:50.247179+00	240	کارشناسی ارشد	جغرافیا و برنامه ریزی گردشگری گرایش برنامه ریزی رویداد و خدمات سفر
def031e7-25fa-42d7-9ac2-e24be7e9367a	2026-05-13 20:38:50.40557+00	396	دکتری	علوم اقتصادی گرایش اقتصاد مالی
0b8fd60d-58e7-49c0-b392-26a316769cf4	2026-05-13 20:39:30.782344+00	397	دکتری	علوم اقتصادی گرایش اقتصاد شهری و منطقه ای
b4e1499e-d1a6-4d10-8aee-1737caf07291	2026-05-13 20:50:56.666433+00	402	دکتری	مدیریت ورزشی
18aaa54a-98db-4473-a5e4-4ccaad7ef3a6	2026-05-13 23:34:41.572533+00	501	دکتری	مدیریت مرز
a9e5ae78-e765-45e8-b75c-c8d624a0bff5	2026-05-13 23:36:24.969555+00	505	دکتری	علوم ارتباطات
db39e7bf-cae6-49d7-9e1f-6965cd4c7b15	2026-05-13 23:39:42.725893+00	509	دکتری	علوم زمین گرایش تکتونیک
97d56455-f68b-469a-a294-997e49e0104e	2026-05-13 23:40:02.121769+00	510	دکتری	علوم زمین گرایش نفت
fdb53bc7-f6ff-4c41-b634-7b324c8b4ce6	2026-05-14 14:22:40.245873+00	608	دکتری	مهندسی هوافضا گرایش دینامیک پرواز و کنترل
05806f2b-2a71-4cef-bb62-521525c15395	2026-05-15 21:47:11.056468+00	680	دکتری	باستان شناسی گرایش دوران اسلامی
\.


--
-- Data for Name: group_members; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_members (id, can_add_members, can_edit_info, can_post_story, can_remove_members, is_archived, is_mandatory, is_muted, is_pinned, joined_at, role, group_id, user_id) FROM stdin;
06543ff6-4c57-42d9-8917-cac67b45324b	f	f	f	f	f	f	f	f	2026-05-15 22:40:41.643657+00	OWNER	de678e17-1960-4bf6-9356-7f84b3c30e2f	e7a11dde-6c43-480b-b0d7-daadd8648e90
5e56c7bc-d3c7-4a65-bc65-02614aafdc08	f	f	f	f	f	f	f	f	2026-05-15 22:41:12.667525+00	OWNER	66cc1222-e911-48df-a715-e8f22bdb7b40	e7a11dde-6c43-480b-b0d7-daadd8648e90
bd97430b-0616-4fcd-aeba-07a2477727b0	f	f	f	f	f	f	f	f	2026-05-15 22:41:28.307929+00	OWNER	3138dcec-1ec2-40ac-a654-2dfa5761bacb	e7a11dde-6c43-480b-b0d7-daadd8648e90
56bb9531-a128-4203-a88a-c4a0f0887648	f	f	f	f	f	f	f	f	2026-05-15 22:41:44.936953+00	OWNER	28ad9898-d414-4cd9-b91d-c05e5c8b844d	e7a11dde-6c43-480b-b0d7-daadd8648e90
f68813eb-9fa1-4851-93ab-e82917726bb1	f	f	f	f	f	f	f	f	2026-05-15 22:47:24.625616+00	OWNER	6ea51eff-7785-4173-96cd-f69499943a51	e7a11dde-6c43-480b-b0d7-daadd8648e90
f31f8510-59f8-483a-84d6-224b6dcaa62e	f	f	f	f	f	f	f	f	2026-05-15 22:47:44.856127+00	OWNER	0afe21b4-bb02-4d06-88da-46c8f792b88f	e7a11dde-6c43-480b-b0d7-daadd8648e90
f6b42d26-e420-4df2-96f1-a9bafc3a8596	f	f	f	f	f	f	f	f	2026-05-15 22:48:04.594516+00	OWNER	a97e0816-5435-4cd8-8030-df1f987e3beb	e7a11dde-6c43-480b-b0d7-daadd8648e90
fe2f6e97-ec12-4dc6-8979-97faf2608ac7	f	f	f	f	f	f	f	f	2026-05-15 22:48:23.712528+00	OWNER	563d7c6c-2cd1-401f-8b93-d1b7c028cca1	e7a11dde-6c43-480b-b0d7-daadd8648e90
abe45051-41bc-423f-b02d-557c35b7a721	f	f	f	f	f	f	f	f	2026-05-15 23:33:08.860265+00	OWNER	7102d9ac-9996-446b-98ca-a9cd2d2071af	e7a11dde-6c43-480b-b0d7-daadd8648e90
39920daa-a9f1-4545-a673-853bf8d7bc56	f	f	f	f	f	f	f	f	2026-05-15 23:33:28.469052+00	OWNER	6b2919b5-b6f1-45d4-a966-b6fcd041a6cc	e7a11dde-6c43-480b-b0d7-daadd8648e90
29836963-2fac-43f4-9064-1c757133b44d	f	f	f	f	f	f	f	f	2026-05-15 23:34:01.501972+00	OWNER	c4d4468f-4c14-4ec1-bccc-30bc4927e64d	e7a11dde-6c43-480b-b0d7-daadd8648e90
bcf3458d-8978-4e86-9082-93779421e807	f	f	f	f	f	f	f	f	2026-05-15 23:34:20.336332+00	OWNER	a247c32c-978d-4f0e-87fb-a0de12bdd531	e7a11dde-6c43-480b-b0d7-daadd8648e90
be4d6c14-2de7-461e-a8e1-5a810b72f75c	f	f	f	f	f	f	f	f	2026-05-15 23:34:37.815904+00	OWNER	8be3a446-6399-404a-8344-9104937f380b	e7a11dde-6c43-480b-b0d7-daadd8648e90
0386717f-9844-4646-bd5e-8f49350505ad	f	f	f	f	f	f	f	f	2026-05-15 23:35:57.760037+00	OWNER	cc10ee0e-bfff-4822-8aaa-1a0e686f42f6	e7a11dde-6c43-480b-b0d7-daadd8648e90
b7de29b2-a139-47ce-ba59-9bac8f381818	f	f	f	f	f	f	f	f	2026-05-15 23:36:12.753574+00	OWNER	9015d40e-3673-4a90-adb8-b5807cdbf388	e7a11dde-6c43-480b-b0d7-daadd8648e90
cd24c58d-5f44-4a2b-8d9d-a566b7d0d60d	f	f	f	f	f	f	f	f	2026-05-15 23:36:27.736944+00	OWNER	591de96c-2adf-4523-9992-ceca74998321	e7a11dde-6c43-480b-b0d7-daadd8648e90
8c2959f8-e646-4cff-af43-c2e35c29fe6c	f	f	f	f	f	f	f	f	2026-05-15 23:36:57.657372+00	OWNER	38d31584-a4d3-467a-b47c-057539322c64	e7a11dde-6c43-480b-b0d7-daadd8648e90
c02569cb-2ba7-433a-814c-1f7ec2bda34f	f	f	f	f	f	f	f	f	2026-05-15 23:43:20.817041+00	OWNER	ac0a9b7c-6d13-4ca7-a0e2-6d89a178bd4b	e7a11dde-6c43-480b-b0d7-daadd8648e90
23d176fc-b634-4e8e-afe0-8fc226275e61	f	f	f	f	f	f	f	f	2026-05-15 23:43:39.653818+00	OWNER	e02ba96c-5b41-4df3-b4df-08d3a6867fb7	e7a11dde-6c43-480b-b0d7-daadd8648e90
29c9dc31-07fd-4528-902e-260a80e86a97	f	f	f	f	f	f	f	f	2026-05-15 23:44:02.984294+00	OWNER	7a3136e7-bf53-4fa2-a56a-b91ac7f5affd	e7a11dde-6c43-480b-b0d7-daadd8648e90
b859b2c9-41f7-4a03-867e-61fc92bb1b5d	f	f	f	f	f	f	f	f	2026-05-15 23:44:20.104489+00	OWNER	d92d1c84-f34d-458a-88a4-e79f7bd8ce18	e7a11dde-6c43-480b-b0d7-daadd8648e90
ba2475cd-ecff-4aed-b662-019e3727fdfb	f	f	f	f	f	f	f	f	2026-05-15 23:44:41.717837+00	OWNER	ce67722a-1cfb-45e5-8b00-3b2b8903d19a	e7a11dde-6c43-480b-b0d7-daadd8648e90
1930a0e7-8b0d-48b5-8c2a-1a8b20cee548	f	f	f	f	f	f	f	f	2026-05-15 23:44:57.718807+00	OWNER	589b18d5-c9cb-43f4-be1b-f3fa8b0a8536	e7a11dde-6c43-480b-b0d7-daadd8648e90
d554fd6c-4221-44e8-bdee-1f57766306f1	f	f	f	f	f	f	f	f	2026-05-15 23:45:13.65669+00	OWNER	04a22619-f758-4c1c-9e46-a2f19feba14b	e7a11dde-6c43-480b-b0d7-daadd8648e90
d54cb84a-7c6d-44ad-97ed-93f53abfb1da	f	f	f	f	f	f	f	f	2026-05-15 23:45:34.357601+00	OWNER	e4bbda30-74aa-4e31-96a5-912f7386df04	e7a11dde-6c43-480b-b0d7-daadd8648e90
89d92796-0578-4a17-bdd5-12729f869dfc	f	f	f	f	f	f	f	f	2026-05-15 23:45:51.264917+00	OWNER	5a5736ff-206b-4414-889c-5e47345c690a	e7a11dde-6c43-480b-b0d7-daadd8648e90
77906cac-3a26-472c-8b62-fdba10260ac6	f	f	f	f	f	f	f	f	2026-05-15 23:46:04.821408+00	OWNER	85fa846e-5933-4743-aa17-44ea7107c57a	e7a11dde-6c43-480b-b0d7-daadd8648e90
ec23630e-b7af-44a6-a2bc-e72adacb15de	f	f	f	f	f	f	f	f	2026-05-15 23:46:18.441885+00	OWNER	3f8c8d6d-e734-4719-b1bf-0db2fe7acdff	e7a11dde-6c43-480b-b0d7-daadd8648e90
02f47dd4-51f8-4048-b2cb-3451f2a12706	f	f	f	f	f	f	f	f	2026-05-15 23:46:36.633099+00	OWNER	b19e80b4-1bd4-4702-b206-680ed26a7f90	e7a11dde-6c43-480b-b0d7-daadd8648e90
758760be-65bb-495c-8c94-ad4da7de5e07	f	f	f	f	f	f	f	f	2026-05-15 23:46:58.765081+00	OWNER	e454e1ea-ea98-4164-aff1-ec83592d8300	e7a11dde-6c43-480b-b0d7-daadd8648e90
6740d338-9917-4e0b-89cb-5dd543e75f05	f	f	f	f	f	f	f	f	2026-05-15 23:47:19.443907+00	OWNER	f2970486-d637-4867-baa0-422a4d9f0bb4	e7a11dde-6c43-480b-b0d7-daadd8648e90
c5a81611-f082-45f3-ba13-af6510803755	f	f	f	f	f	f	f	f	2026-05-15 23:47:38.473694+00	OWNER	4db73d90-805b-4c4e-96c0-ca388c042787	e7a11dde-6c43-480b-b0d7-daadd8648e90
2ede8602-238e-49d5-8286-be4e09c8fa92	f	f	f	f	f	f	f	f	2026-05-15 23:47:54.459145+00	OWNER	6ba3f308-89f4-4d94-9c2b-a6a91d3816b5	e7a11dde-6c43-480b-b0d7-daadd8648e90
e1d62a99-2aad-4dcc-bff3-c3a3c2f0c0de	f	f	f	f	f	f	f	f	2026-05-15 23:48:17.501583+00	OWNER	0bd10975-02cc-4bf7-9065-fb3321714daf	e7a11dde-6c43-480b-b0d7-daadd8648e90
6b98a420-223b-45d1-9980-0337f93fa13d	f	f	f	f	f	f	f	f	2026-05-15 23:48:36.635707+00	OWNER	cab5ac49-062b-4cac-843b-e03744488b20	e7a11dde-6c43-480b-b0d7-daadd8648e90
defb5d71-ee66-47d4-9feb-2df012b98ef3	f	f	f	f	f	f	f	f	2026-05-15 23:48:53.934513+00	OWNER	8171ece2-78c2-443e-a505-c4fc59a39a94	e7a11dde-6c43-480b-b0d7-daadd8648e90
0820cd26-bff9-4e5c-b327-a0ad7067d53f	f	f	f	f	f	f	f	f	2026-05-15 23:49:07.993236+00	OWNER	7f341fb4-1fd1-482b-9595-982204d8e874	e7a11dde-6c43-480b-b0d7-daadd8648e90
220d8ff4-a964-4a62-aa05-5744c663316e	f	f	f	f	f	f	f	f	2026-05-15 23:49:38.41393+00	OWNER	4c3a5de1-04c6-41d7-99a6-c2538fc01e88	e7a11dde-6c43-480b-b0d7-daadd8648e90
0ed20a8a-5999-4a13-9e2a-8f7f05f56b6b	f	f	f	f	f	f	f	f	2026-05-15 23:49:57.22047+00	OWNER	88d31647-3fd2-4d9c-ae5b-1bbd3ce60dca	e7a11dde-6c43-480b-b0d7-daadd8648e90
43ad37a0-791f-420d-b4ed-5e12c9f31c4d	f	f	f	f	f	f	f	f	2026-05-15 23:50:13.835647+00	OWNER	31e6862c-969f-4ada-bdf7-1e3bc0929a5b	e7a11dde-6c43-480b-b0d7-daadd8648e90
2ffe00ad-076d-4261-8005-6c1f0a07c43f	f	f	f	f	f	f	f	f	2026-05-15 23:50:34.934784+00	OWNER	13934e9c-9dfd-4b23-a6f1-56832d1e368b	e7a11dde-6c43-480b-b0d7-daadd8648e90
69c0674f-8992-4778-bb0a-f4c7b0cf3041	f	f	f	f	f	f	f	f	2026-05-15 23:50:54.062255+00	OWNER	07fff2e0-77f9-4a46-8196-420f95affacb	e7a11dde-6c43-480b-b0d7-daadd8648e90
c593ee8a-f24c-475f-a7b6-98ba3e25fc1b	f	f	f	f	f	f	f	f	2026-05-15 23:51:14.239653+00	OWNER	c058d8e1-ab1e-4fa9-93ff-2f261ae4277b	e7a11dde-6c43-480b-b0d7-daadd8648e90
897f49ff-1f45-4671-ba2c-a403befbbcff	f	f	f	f	f	f	f	f	2026-05-15 23:51:35.777776+00	OWNER	76429f9a-24b0-4fba-b4e6-28db6244562b	e7a11dde-6c43-480b-b0d7-daadd8648e90
f1bdcb91-3b97-4714-a073-3ec3686856c4	f	f	f	f	f	f	f	f	2026-05-15 23:51:51.039267+00	OWNER	d9bb3394-8083-4448-bacb-b551ffb534de	e7a11dde-6c43-480b-b0d7-daadd8648e90
3ada1212-57d6-429d-a8b3-408ec3d71798	f	f	f	f	f	f	f	f	2026-05-15 23:52:07.511941+00	OWNER	e87e4c5f-2ef9-4799-a751-a577cc44cd9a	e7a11dde-6c43-480b-b0d7-daadd8648e90
6ad68749-a86d-4e9d-bad1-4b69461f55c4	f	f	f	f	f	f	f	f	2026-05-15 23:52:22.691776+00	OWNER	8273735a-9745-4010-ac3e-bc6a683c6b15	e7a11dde-6c43-480b-b0d7-daadd8648e90
f5319c69-50e4-4f82-8150-6d61ba331fd5	f	f	f	f	f	f	f	f	2026-05-15 23:52:38.980395+00	OWNER	e3a88983-2d1c-4bc9-8377-0037963da30b	e7a11dde-6c43-480b-b0d7-daadd8648e90
1e8f490d-73b2-4d42-b65a-ebe7d30bb95c	f	f	f	f	f	f	f	f	2026-05-15 23:54:58.471684+00	OWNER	06dd7d73-5c4d-45a3-8570-77a3b7853880	e7a11dde-6c43-480b-b0d7-daadd8648e90
468aef5b-4342-452b-8415-3279fe5907d5	f	f	f	f	f	f	f	f	2026-05-15 23:56:24.827508+00	OWNER	27dcb383-e3df-43c4-8dbd-7f985be4bfab	e7a11dde-6c43-480b-b0d7-daadd8648e90
\.


--
-- Data for Name: group_message_amplitudes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_message_amplitudes (group_message_id, amplitudes) FROM stdin;
\.


--
-- Data for Name: group_message_reactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_message_reactions (id, created_at, reaction, message_id, user_id) FROM stdin;
\.


--
-- Data for Name: group_messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.group_messages (id, content, created_at, edited_at, forwarded_from, is_edited, is_pinned, media_url, pinned_at, pinned_by_id, scheduled_at, type, group_id, poll_id, reply_to_id, sender_id, action_label, action_url, timer_target_at) FROM stdin;
\.


--
-- Data for Name: groups; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.groups (id, allow_members_to_edit_info, allow_members_to_send_messages, avatar_url, created_at, description, display_mode, hide_members, invite_link, is_invite_link_enabled, is_official, is_public, name, official_category, target_city, target_education_level, target_field_of_study, target_province, target_university, created_by, target_ministry, target_audience_type) FROM stdin;
de678e17-1960-4bf6-9356-7f84b3c30e2f	f	t	\N	2026-05-15 22:40:41.597131+00		SPECIAL	t	\N	t	t	t	گروه رسمی دانشجویان ایران زمین 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
66cc1222-e911-48df-a715-e8f22bdb7b40	f	t	\N	2026-05-15 22:41:12.651443+00		SPECIAL	t	\N	t	t	t	گروه پژوهش و تحقیقات 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
3138dcec-1ec2-40ac-a654-2dfa5761bacb	f	t	\N	2026-05-15 22:41:28.290344+00		SPECIAL	t	\N	t	t	t	گروه پرسش و پاسخ علمی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
28ad9898-d414-4cd9-b91d-c05e5c8b844d	f	t	\N	2026-05-15 22:41:44.902473+00		SPECIAL	t	\N	t	t	t	گروه انتقادات و پیشنهادات کلاسور	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
6ea51eff-7785-4173-96cd-f69499943a51	f	t	\N	2026-05-15 22:47:24.595032+00		SPECIAL	t	\N	t	t	t	گروه دانش آموزان تجربی 	STUDENTS_IRAN	\N	متوسطه دوم (نظری)	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
0afe21b4-bb02-4d06-88da-46c8f792b88f	f	t	\N	2026-05-15 22:47:44.839473+00		SPECIAL	t	\N	t	t	t	گروه دانش آموزان ریاضی 	STUDENTS_IRAN	\N	متوسطه دوم (نظری)	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
a97e0816-5435-4cd8-8030-df1f987e3beb	f	t	\N	2026-05-15 22:48:04.576737+00		SPECIAL	t	\N	t	t	t	گروه دانش آموزان انسانی	STUDENTS_IRAN	\N	متوسطه دوم (نظری)	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
563d7c6c-2cd1-401f-8b93-d1b7c028cca1	f	t	\N	2026-05-15 22:48:23.688557+00		SPECIAL	t	\N	t	t	t	گروه دانش آموزان هنر 	STUDENTS_IRAN	\N	هنرستان	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
6b2919b5-b6f1-45d4-a966-b6fcd041a6cc	f	t	\N	2026-05-15 23:33:28.433734+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان وزارت بهداشت 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	وزارت بهداشت	\N
7102d9ac-9996-446b-98ca-a9cd2d2071af	f	t	\N	2026-05-15 23:33:08.808169+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان وزارت علوم 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
c4d4468f-4c14-4ec1-bccc-30bc4927e64d	f	t	\N	2026-05-15 23:34:01.483322+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه پیام نور 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	پیام نور	\N
a247c32c-978d-4f0e-87fb-a0de12bdd531	f	t	\N	2026-05-15 23:34:20.314586+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه آزاد 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	دانشگاه آزاد	\N
8be3a446-6399-404a-8344-9104937f380b	f	t	\N	2026-05-15 23:34:37.801316+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه فنی و حرفه‌ای 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	فنی حرفه ای	\N
cc10ee0e-bfff-4822-8aaa-1a0e686f42f6	f	t	\N	2026-05-15 23:35:57.738333+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه منابع طبیعی	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	منابع طبیعی	\N
9015d40e-3673-4a90-adb8-b5807cdbf388	f	t	\N	2026-05-15 23:36:12.727107+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه علمی کاربردی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	علمی کاربردی	\N
591de96c-2adf-4523-9992-ceca74998321	f	t	\N	2026-05-15 23:36:27.710289+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه فرهنگیان 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	فرهنگیان	\N
38d31584-a4d3-467a-b47c-057539322c64	f	t	\N	2026-05-15 23:36:57.567826+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه هنر	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	هنر	\N
ac0a9b7c-6d13-4ca7-a0e2-6d89a178bd4b	f	t	\N	2026-05-15 23:43:20.796492+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استاد آذربایجان شرقی 	STUDENTS_IRAN	\N	\N	\N	آذربایجان شرقی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e02ba96c-5b41-4df3-b4df-08d3a6867fb7	f	t	\N	2026-05-15 23:43:39.635719+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان آذربایجان غربی 	STUDENTS_IRAN	\N	\N	\N	آذربایجان غربی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
7a3136e7-bf53-4fa2-a56a-b91ac7f5affd	f	t	\N	2026-05-15 23:44:02.965656+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان اصفهان 	STUDENTS_IRAN	\N	\N	\N	اصفهان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
d92d1c84-f34d-458a-88a4-e79f7bd8ce18	f	t	\N	2026-05-15 23:44:20.08661+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان اردبیل	STUDENTS_IRAN	\N	\N	\N	اردبیل	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
ce67722a-1cfb-45e5-8b00-3b2b8903d19a	f	t	\N	2026-05-15 23:44:41.693397+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان البرز	STUDENTS_IRAN	\N	\N	\N	البرز	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
589b18d5-c9cb-43f4-be1b-f3fa8b0a8536	f	t	\N	2026-05-15 23:44:57.701483+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان ایلام	STUDENTS_IRAN	\N	\N	\N	ایلام	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
04a22619-f758-4c1c-9e46-a2f19feba14b	f	t	\N	2026-05-15 23:45:13.631589+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان بوشهر	STUDENTS_IRAN	\N	\N	\N	بوشهر	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e4bbda30-74aa-4e31-96a5-912f7386df04	f	t	\N	2026-05-15 23:45:34.321687+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان تهران 	STUDENTS_IRAN	\N	\N	\N	تهران	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
5a5736ff-206b-4414-889c-5e47345c690a	f	t	\N	2026-05-15 23:45:51.246039+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان خراسان جنوبی 	STUDENTS_IRAN	\N	\N	\N	خراسان جنوبی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
85fa846e-5933-4743-aa17-44ea7107c57a	f	t	\N	2026-05-15 23:46:04.647174+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان خراسان رضوی 	STUDENTS_IRAN	\N	\N	\N	خراسان رضوی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
3f8c8d6d-e734-4719-b1bf-0db2fe7acdff	f	t	\N	2026-05-15 23:46:18.425542+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان خراسان شمالی 	STUDENTS_IRAN	\N	\N	\N	خراسان شمالی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
b19e80b4-1bd4-4702-b206-680ed26a7f90	f	t	\N	2026-05-15 23:46:36.616475+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان خوزستان 	STUDENTS_IRAN	\N	\N	\N	خوزستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e454e1ea-ea98-4164-aff1-ec83592d8300	f	t	\N	2026-05-15 23:46:58.748323+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان زنجان	STUDENTS_IRAN	\N	\N	\N	زنجان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
f2970486-d637-4867-baa0-422a4d9f0bb4	f	t	\N	2026-05-15 23:47:19.418505+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان سمنان	STUDENTS_IRAN	\N	\N	\N	سمنان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
4db73d90-805b-4c4e-96c0-ca388c042787	f	t	\N	2026-05-15 23:47:38.460025+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان سیستان و بلوچستان 	STUDENTS_IRAN	\N	\N	\N	سیستان وبلوچستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
6ba3f308-89f4-4d94-9c2b-a6a91d3816b5	f	t	\N	2026-05-15 23:47:54.440322+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان فارس	STUDENTS_IRAN	\N	\N	\N	فارس	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
0bd10975-02cc-4bf7-9065-fb3321714daf	f	t	\N	2026-05-15 23:48:17.477033+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان قزوین	STUDENTS_IRAN	\N	\N	\N	قزوین	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
cab5ac49-062b-4cac-843b-e03744488b20	f	t	\N	2026-05-15 23:48:36.620606+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان قم	STUDENTS_IRAN	\N	\N	\N	قم	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
8171ece2-78c2-443e-a505-c4fc59a39a94	f	t	\N	2026-05-15 23:48:53.919616+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان لرستان	STUDENTS_IRAN	\N	\N	\N	لرستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
7f341fb4-1fd1-482b-9595-982204d8e874	f	t	\N	2026-05-15 23:49:07.97211+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان مازندران 	STUDENTS_IRAN	\N	\N	\N	مازندران	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
4c3a5de1-04c6-41d7-99a6-c2538fc01e88	f	t	\N	2026-05-15 23:49:38.38891+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان مرکزی 	STUDENTS_IRAN	\N	\N	\N	مرکزی	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
88d31647-3fd2-4d9c-ae5b-1bbd3ce60dca	f	t	\N	2026-05-15 23:49:57.204356+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان هرمزگان 	STUDENTS_IRAN	\N	\N	\N	هرمزگان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
31e6862c-969f-4ada-bdf7-1e3bc0929a5b	f	t	\N	2026-05-15 23:50:13.819499+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان همدان 	STUDENTS_IRAN	\N	\N	\N	همدان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
13934e9c-9dfd-4b23-a6f1-56832d1e368b	f	t	\N	2026-05-15 23:50:34.896846+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان چهارمحال و بختیاری 	STUDENTS_IRAN	\N	\N	\N	چهارمحال وبختیاری	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
07fff2e0-77f9-4a46-8196-420f95affacb	f	t	\N	2026-05-15 23:50:54.039698+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان کردستان 	STUDENTS_IRAN	\N	\N	\N	کردستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e3a88983-2d1c-4bc9-8377-0037963da30b	f	t	\N	2026-05-15 23:52:38.96395+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان یزد 	STUDENTS_IRAN	\N	\N	\N	یزد	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
c058d8e1-ab1e-4fa9-93ff-2f261ae4277b	f	t	\N	2026-05-15 23:51:14.216359+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان کرمان	STUDENTS_IRAN	\N	\N	\N	کرمان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
76429f9a-24b0-4fba-b4e6-28db6244562b	f	t	\N	2026-05-15 23:51:35.761867+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان کرمانشاه 	STUDENTS_IRAN	\N	\N	\N	کرمانشاه	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
d9bb3394-8083-4448-bacb-b551ffb534de	f	t	\N	2026-05-15 23:51:51.019608+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان کهگیلویه و بویراحمد 	STUDENTS_IRAN	\N	\N	\N	کهگیلویه وبویراحمد	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
e87e4c5f-2ef9-4799-a751-a577cc44cd9a	f	t	\N	2026-05-15 23:52:07.498993+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان گلستان 	STUDENTS_IRAN	\N	\N	\N	گلستان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
8273735a-9745-4010-ac3e-bc6a683c6b15	f	t	\N	2026-05-15 23:52:22.677746+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های استان گیلان 	STUDENTS_IRAN	\N	\N	\N	گیلان	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	\N	\N
06dd7d73-5c4d-45a3-8570-77a3b7853880	f	t	\N	2026-05-15 23:54:58.45245+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان علوم قرآن و معارف 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	علوم قرآن و معارف	\N
27dcb383-e3df-43c4-8dbd-7f985be4bfab	f	t	\N	2026-05-15 23:56:24.797469+00		SPECIAL	t	\N	t	t	t	گروه دانشجویان دانشگاه های غیرانتفاعی 	STUDENTS_IRAN	\N	\N	\N	\N	\N	e7a11dde-6c43-480b-b0d7-daadd8648e90	غیرانتفاعی	\N
\.


--
-- Data for Name: hashtag_promotions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.hashtag_promotions (id, content_text, created_at, moderated_at, moderated_by, moderation_status, published_at, rejection_reason, subscription_id, hashtag_id, user_id) FROM stdin;
\.


--
-- Data for Name: home_banners; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.home_banners (id, color_end, color_start, created_at, display_order, image_url, is_active, link_url, section, title) FROM stdin;
b03696f6-d94b-4fea-aa18-ce7e1d41d27d	4293673082	4284612842	2026-05-08 09:23:28.116637+00	0	https://example.com/banner1.jpg	t	https://kelasor.com/promo1	MOSBAT_ELM	جشنواره تابستانه مثبت علم
c46e0874-eee7-4e7f-82ca-7008adb87e6b	4293673082	4284612842	2026-05-08 09:23:28.116656+00	0	https://example.com/banner2.jpg	t	https://kelasor.com/kotlin	MOSBAT_ELM	دوره جامع برنامه نویسی کاتلین
\.


--
-- Data for Name: institution_admins; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_admins (institution_id, admin_id) FROM stdin;
\.


--
-- Data for Name: institution_clubs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_clubs (institution_id, club_id) FROM stdin;
\.


--
-- Data for Name: institution_faculties; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_faculties (institution_id, faculty_name) FROM stdin;
\.


--
-- Data for Name: institution_fields; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_fields (institution_id, field_id) FROM stdin;
\.


--
-- Data for Name: institution_honors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_honors (id, created_at, date, description, image_url, title, institution_id) FROM stdin;
\.


--
-- Data for Name: institution_instructors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_instructors (institution_id, instructor_id) FROM stdin;
\.


--
-- Data for Name: institution_manual_instructors; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_manual_instructors (institution_id, avatar_url, name, resume) FROM stdin;
\.


--
-- Data for Name: institution_specialties; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_specialties (institution_id, specialty) FROM stdin;
\.


--
-- Data for Name: institution_student_orgs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_student_orgs (institution_id, org_id) FROM stdin;
\.


--
-- Data for Name: institution_universities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institution_universities (institution_id, university_name) FROM stdin;
\.


--
-- Data for Name: institutions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.institutions (id, address, admin_note, city, contact_email, contact_phone, created_at, description, is_active, logo_url, name, province, registration_number, reviewed_at, reviewed_by, type, updated_at, verification_status, channel_id, owner_user_id, average_rating, dependency_description, is_subsidiary, review_count) FROM stdin;
\.


--
-- Data for Name: locked_contents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.locked_contents (id, content_type, created_at, description, encryption_key, lock_status, price_rials, purchase_count, storage_key, thumbnail_url, title, updated_at, view_count, channel_id, uploader_id) FROM stdin;
\.


--
-- Data for Name: message_amplitudes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.message_amplitudes (message_id, amplitudes) FROM stdin;
\.


--
-- Data for Name: message_reactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.message_reactions (id, created_at, reaction, message_id, user_id) FROM stdin;
\.


--
-- Data for Name: messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.messages (id, content, created_at, edited_at, forwarded_from, is_edited, is_pinned, media_url, pinned_at, pinned_by_id, scheduled_at, status, type, chat_id, poll_id, reply_to_id, sender_id, action_label, action_url, timer_target_at) FROM stdin;
\.


--
-- Data for Name: notifications; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.notifications (id, actor_avatar_url, actor_id, actor_name, body, created_at, is_read, is_subscription_notification, notification_tier, related_entity_id, title, type, user_id) FROM stdin;
\.


--
-- Data for Name: official_hashtags; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.official_hashtags (id, branch_channel_id, category, created_at, display_name_fa, is_active, national_channel_id, tag, university_channel_id) FROM stdin;
\.


--
-- Data for Name: otp_codes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.otp_codes (id, code, created_at, expires_at, is_used, phone_number) FROM stdin;
d3f105f4-40b2-4004-9513-d56370995600	369390	2026-05-12 02:41:50.905185+00	2026-05-12 02:46:50.905117+00	f	09123456789
b177777d-c744-440f-b72b-df7f26b60c42	741785	2026-05-12 02:44:51.201311+00	2026-05-12 02:49:51.201279+00	f	09309663349
95cfb009-b51f-4f8d-9ab5-2716d3e8b9cb	715637	2026-05-12 02:54:20.808838+00	2026-05-12 02:59:20.808782+00	f	09309663349
34a9d573-6add-4bf5-8cdb-910c405397d8	978238	2026-05-12 02:57:55.144627+00	2026-05-12 03:02:55.144603+00	f	09309663349
2191873f-3b7d-40ec-9863-e412f9531914	168449	2026-05-12 02:58:24.605505+00	2026-05-12 03:03:24.605484+00	f	09123456789
9b0a713a-def5-4aae-a4dd-06cafa152bf8	731802	2026-05-12 03:02:42.464547+00	2026-05-12 03:07:42.464514+00	f	09123456789
86d967cc-e833-4a23-b1db-3b930d83abff	320454	2026-05-12 04:13:55.281699+00	2026-05-12 04:18:55.281603+00	t	09309663349
bdb66078-e544-4f9c-a468-5d908ddcc645	712599	2026-05-12 11:25:04.550143+00	2026-05-12 11:30:04.550085+00	f	09309663379
24eb749a-5d3e-4357-b858-75ceae348301	249585	2026-05-12 11:25:21.289821+00	2026-05-12 11:30:21.289804+00	t	09309663349
e568bc1d-4bfc-41a9-90a6-7c717b642962	511374	2026-05-12 13:18:14.030081+00	2026-05-12 13:23:14.030055+00	f	09331832923
3a7822d7-1fba-465a-aad8-c4c198b02923	566123	2026-05-12 13:23:29.825142+00	2026-05-12 13:28:29.824552+00	f	09331832923
4fc7feab-736b-4267-8e50-0669cfe76533	117211	2026-05-13 18:36:32.056689+00	2026-05-13 18:41:32.056619+00	f	09309663349
7ce72090-38e5-4c94-951f-70ad31fc42bd	732316	2026-05-13 18:39:53.125837+00	2026-05-13 18:44:53.125786+00	f	09309663349
b15b5b98-0ba6-4a87-9b8b-5b61d8c408e6	881931	2026-05-13 18:43:29.022202+00	2026-05-13 18:48:29.022161+00	f	09309663349
d6e37587-e070-4839-a104-49f0d5b488ad	474167	2026-05-13 18:57:14.257809+00	2026-05-13 19:02:14.257743+00	f	09309663349
1359312f-b30a-417d-9fdf-eaee2ea69cb0	561143	2026-05-13 18:57:21.734598+00	2026-05-13 19:02:21.73371+00	f	09309663349
18ecff6e-7a85-4c11-a06a-e4eb1b16bf61	805533	2026-05-13 18:57:22.631305+00	2026-05-13 19:02:22.631212+00	f	09309663349
e82d3815-e271-435e-8524-c525c8544a6d	762032	2026-05-13 18:57:30.979723+00	2026-05-13 19:02:30.979707+00	f	09309663349
afd6a761-cf64-482a-8a7b-0668f59a2644	281152	2026-05-13 18:57:37.093446+00	2026-05-13 19:02:37.093436+00	f	09309663349
a5df33cd-4b54-46e5-9b89-9c8912767848	979624	2026-05-13 18:58:21.879163+00	2026-05-13 19:03:21.879139+00	f	09309663349
320115df-cb3c-4623-8c65-d6f7a2d13d72	589118	2026-05-13 19:00:01.809167+00	2026-05-13 19:05:01.809108+00	f	09309663349
e2ddd522-ac0f-40cc-a5e1-519cd8fa3254	539745	2026-05-13 19:02:47.043662+00	2026-05-13 19:07:47.043578+00	t	09309663349
\.


--
-- Data for Name: panel_admin_permissions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.panel_admin_permissions (admin_id, permission) FROM stdin;
\.


--
-- Data for Name: panel_admins; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.panel_admins (id, created_at, display_name, is_super_admin, password_hash, username) FROM stdin;
dbb79702-f056-4b68-8a07-acb7e105e90b	2026-05-08 06:53:52.321638+00	مدیر اصلی	t	$2a$10$FxUlpkWB9tQWC2n5Z0jR0.n1xkWKGSolnQwkiVknGtpmxKqG7UAAC	admin
21edaee5-e6e9-4231-b337-96b9db748235	2026-05-08 13:32:29.309586+00	ملیکا خانم منصوری	t	$2a$10$.YLNbjTctVSHlVeElYxwaujReoCRD/EJTI7ta2/hT6Oj.iESFUGeq	Melika810906
9835f66b-fa8c-4191-9302-946864372d2d	2026-05-08 13:34:08.920491+00	دانیال اصفهونی	t	$2a$10$HsepCLeTaSWxCbbXDf7ZFOt7U8zb12ERzDV7nZkojqyvlfLWXxKBu	danykhezry02
ad63c91e-4c00-48be-96d2-9b4678e62704	2026-05-12 20:58:47.123254+00	فاطمه منصوری نسب	t	$2a$10$aNKSilQ.LzFmbSfcVOHcy.q/hlM4PSIW.FKGpkLAwB1DjC.A4fMxW	fatememansourinasab
\.


--
-- Data for Name: poll_options; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.poll_options (id, text, vote_count, poll_id) FROM stdin;
\.


--
-- Data for Name: poll_votes; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.poll_votes (id, voted_at, option_id, poll_id, user_id) FROM stdin;
\.


--
-- Data for Name: polls; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.polls (id, created_at, is_anonymous, is_multiple_choice, question, creator_id) FROM stdin;
\.


--
-- Data for Name: promotion_media_urls; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.promotion_media_urls (promotion_id, media_url) FROM stdin;
\.


--
-- Data for Name: refresh_tokens; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.refresh_tokens (id, created_at, expires_at, is_revoked, session_id, token, user_id) FROM stdin;
9f247cbd-28ac-4b90-bb6b-8278547d9cb5	2026-05-12 04:14:14.723756+00	2026-06-11 04:14:14.723751+00	f	1117ae68-c2fb-4c7c-a252-28a1f54021b9	6e2fb0d4-33b8-4f5d-bb45-451538076373	e7a11dde-6c43-480b-b0d7-daadd8648e90
b4fa1120-d744-460a-9900-430ca0d867dd	2026-05-12 11:25:28.963397+00	2026-06-11 11:25:28.963395+00	f	c0e2c54e-f9ea-4671-b9df-5fb1c1d583f9	58e1ecb9-cf90-4b3c-bbbe-ce5d9855f55e	e7a11dde-6c43-480b-b0d7-daadd8648e90
08ba045f-5542-4bc4-a077-23a96fffd22a	2026-05-13 19:03:02.809043+00	2026-06-12 19:03:02.809036+00	f	dcc65bf4-3b28-40cb-9229-4f023250bc2e	29d69410-ac32-4a6c-8d08-797017fa86b5	e7a11dde-6c43-480b-b0d7-daadd8648e90
\.


--
-- Data for Name: riddle_options; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.riddle_options (id, display_order, text, riddle_id) FROM stdin;
\.


--
-- Data for Name: role_channel_mappings; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.role_channel_mappings (id, created_at, educational_role, grade_level, major, channel_id) FROM stdin;
\.


--
-- Data for Name: smart_folder_rules; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.smart_folder_rules (id, classification, display_order, folder_type, icon_name, is_active, label_fa) FROM stdin;
\.


--
-- Data for Name: startup_ideas; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.startup_ideas (id, contact_info, created_at, description, title, user_id) FROM stdin;
\.


--
-- Data for Name: stories; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.stories (id, caption, created_at, duration_seconds, expires_at, media_url, type, channel_id, group_id, user_id) FROM stdin;
\.


--
-- Data for Name: story_replies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.story_replies (id, content, created_at, story_id, user_id) FROM stdin;
\.


--
-- Data for Name: story_views; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.story_views (id, viewed_at, story_id, user_id) FROM stdin;
\.


--
-- Data for Name: student_orgs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.student_orgs (id, display_order, name) FROM stdin;
9bb26565-5325-4f63-9e0f-03dd4b495a5f	0	بسیج دانشجویی
b316b48b-f24a-4c54-8738-e7b2501ed222	0	انجمن اسلامی
bdbdd570-cc1a-4307-876f-86f01dda436f	0	شورای صنفی
\.


--
-- Data for Name: subscription_plans; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.subscription_plans (id, created_at, duration_days, features, is_active, max_promotions, name, price_rials, tier) FROM stdin;
\.


--
-- Data for Name: teacher_verif_documents; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.teacher_verif_documents (request_id, document_url) FROM stdin;
\.


--
-- Data for Name: teacher_verification_requests; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.teacher_verification_requests (id, admin_note, created_at, full_name, institution, national_code, reviewed_at, reviewed_by, status, teaching_field, updated_at, user_id) FROM stdin;
\.


--
-- Data for Name: universities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.universities (id, article_count, city, country, created_at, departments, established_year, facilities, faculties, honors, image_url, iran_rank, journal_count, last_admission_capacity, latitude, longitude, ministry_name, name, paper_count, professor_count, professor_names, province, publication_count, student_count, student_orgs, type, website_url, world_rank, rankings) FROM stdin;
1fb9db1d-a158-4c02-8726-ba0b59c20e36	0	اصفهان	ایران	2026-05-16 00:01:02.211648+00	ادبیات و علوم انسانی:تاریخ (کارشناسی),زبان و ادبیات فارسی (کارشناسی),جامعه شناسی (کارشناسی),مددکاری اجتماعی (کارشناسی),فلسفه (کارشناسی),تاریخ گرایش تاریخ ایران اسلامی (کارشناسی ارشد),تاریخ گرایش تاریخ اسلام (کارشناسی ارشد),تاریخ گرایش مطالعات خلیج فارس (کارشناسی ارشد),تاریخ فرهنگ و تمدن اسلامی (کارشناسی ارشد),شیعه شناسی گرایش تاریخ (کارشناسی ارشد),تاریخ علم گرایش طب و داروسازی در جهان اسلام (کارشناسی ارشد),تاریخ گرایش تاریخ ایران باستان (کارشناسی ارشد),زبان و ادبیات فارسی (کارشناسی ارشد),زبان و ادبیات فارسی گرایش ادبیات پایداری (کارشناسی ارشد),زبان و ادبیات فارسی گرایش ویرایش و نگارش (کارشناسی ارشد),زبان و ادبیات فارسی گرایش آموزش زبان فارسی (کارشناسی ارشد),زبان و ادبیات فارسی گرایش ادبیات عامه (کارشناسی ارشد),جامعه شناسی (کارشناسی ارشد),مطالعات زنان گرایش زن و خانواده (کارشناسی ارشد),شیعه شناسی گرایش جامعه شناسی (کارشناسی ارشد),مدیریت راهبردی فرهنگ (کارشناسی ارشد),مطالعات فرهنگی و رسانه (کارشناسی ارشد),جمعیت شناسی (کارشناسی ارشد),فلسفه (کارشناسی ارشد),منطق (کارشناسی ارشد),فلسفه علم (کارشناسی ارشد),تاریخ گرایش تاریخ ایران بعد از اسلام (دکتری),تاریخ گرایش تاریخ اسلام (دکتری),تاریخ گرایش تاریخ محلی (دکتری),تاریخ انقلاب اسلامی (دکتری),تاریخ تشیع اثنی عشری (دکتری),تاریخ گرایش تاریخ ایران قبل از اسلام (دکتری),زبان و ادبیات فارسی (دکتری),زبان و ادبیات فارسی گرایش ادبیات حماسی (دکتری),زبان و ادبیات فارسی گرایش ادبیات غنایی (دکتری),زبان و ادبیات فارسی گرایش ادبیات عرفانی (دکتری),زبان و ادبیات فارسی گرایش آموزش زبان فارسی (دکتری),جامعه شناسی گرایش جامعه شناسی اقتصادی و توسعه (دکتری),جامعه شناسی گرایش جامعه شناسی مسائل اجتماعی ایران (دکتری),جامعه شناسی گرایش جامعه شناسی فرهنگی (دکتری),فلسفه محض (دکتری)|الهیات و معارف اهل بیت (ع):علوم قرآن و حدیث (کارشناسی)|ریاضی و آمار:آمار (کارشناسی)|زبان های خارجی:زبان و ادبیات انگلیسی (کارشناسی)|شیمی:شیمی گرایش شیمی آلی (کارشناسی ارشد)|علوم اداری و اقتصاد:اقتصاد (کارشناسی)|علوم تربیتی و روانشناسی:علوم تربیتی (کارشناسی)|علوم جغرافیایی و برنامه ریزی:گردشگری (کارشناسی)|علوم:زمین شناسی (کارشناسی)|علوم ورزشی:علوم ورزشی (کارشناسی)|علوم و فناوری های زیستی:زیست شناسی گیاهی (کارشناسی)|فنی و مهندسی:مهندسی برق (کارشناسی)|فیزیک:فیزیک (کارشناسی)|مهندسی عمران و حمل و نقل:مهندسی خط و سازه های ریلی (کارشناسی)|مهندسی کامپیوتر:مهندسی کامپیوتر (کارشناسی)|ریاضی و کامپیوتر پردیس خوانسار:آمار (کارشناسی)|مرکز آموزش عالی پردیس شهرضا:ریاضیات و کاربرد ها (کارشناسی)	1325		ادبیات و علوم انسانی, الهیات و معارف اهل بیت (ع), ریاضی و آمار, زبان های خارجی, شیمی, علوم اداری و اقتصاد, علوم تربیتی و روانشناسی, علوم جغرافیایی و برنامه ریزی, علوم, علوم ورزشی, علوم و فناوری های زیستی, فنی و مهندسی, فیزیک, مهندسی عمران و حمل و نقل, مهندسی کامپیوتر, ریاضی و کامپیوتر پردیس خوانسار, مرکز آموزش عالی پردیس شهرضا			0	0		35.6892	51.389	وزارت علوم	دانشگاه اصفهان	0	745		اصفهان	0	17000		دولتی		0	\N
451d0f72-14c2-487f-a64a-5ab990cbdcba	0	اردبیل	ایران	2026-05-15 21:37:33.254687+00	ادبیات و زبانهای خارجی:زبان و ادبیات فارسی (کارشناسی),زبان و ادبیات فارسی (کارشناسی ارشد),آموزش زبان انگلیسی (کارشناسی),زبان و ادبیات فارسی (دکتری),زبان و ادبیات عربی (کارشناسی)|ادبیات و علوم انسانی:باستان شناسی گرایش پیش از تاریخ (دکتری),روان شناسی عمومی (دکتری),باستان شناسی گرایش دوران اسلامی (دکتری),باستان شناسی گرایش دوران تاریخی (دکتری),تربیت بدنی و علوم ورزشی گرایش فیزیولوژی ورزشی (دکتری),جغرافیا و برنامه ریزی شهری (دکتری),علوم جغرافیایی گرایش ژئومورفولوژی (دکتری),علوم جغرافیایی گرایش طبیعی/اقلیم شناسی (دکتری),روانشناسی عمومی (کارشناسی),تربیت بدنی و علوم ورزشی (کارشناسی),آب و هواشناسی (کارشناسی),ژئومورفولوژی (کارشناسی),الهیات و معارف اسلامی گرایش ادیان و عرفان (کارشناسی),باستان شناسی (کارشناسی),راهنمایی و مشاوره (کارشناسی),روانشناسی بالینی (کارشناسی),صنایع دستی (کارشناسی),مدیریت صنعتی (کارشناسی),مدیریت جهانگردی (کارشناسی),جغرافیا و برنامه ریزی شهری (کارشناسی),روان شناسی عمومی (کارشناسی ارشد),تربیت بدنی و علوم ورزشی گرایش فیزیولوژی ورزشی (کارشناسی ارشد),جغرافیای طبیعی گرایش اقلیم شناسی در برنامه ریزی محیطی (کارشناسی ارشد),جغرافیای طبیعی گرایش ژئومورفولوژی (کارشناسی ارشد),باستان شناسی (کارشناسی ارشد),روان شناسی بالینی (کارشناسی ارشد),جغرافیا و برنامه ریزی شهری (کارشناسی ارشد),عرفان اسلامی (کارشناسی ارشد),تحقیقات آموزشی (کارشناسی ارشد)|علوم پایه:زیست شناسی سلولی و مولکولی (دکتری),شیمی گرایش شیمی آلی (دکتری),شیمی گرایش شیمی تجزیه (دکتری),شیمی گرایش شیمی فیزیک (دکتری),فیزیک (کارشناسی),شیمی کاربردی (کارشناسی),زیست شناسی گیاهی (کارشناسی),زیست شناسی سلولی و مولکولی (کارشناسی),زیست شناسی عمومی (کارشناسی),شیمی گرایش شیمی آلی (کارشناسی ارشد),شیمی گرایش شیمی فیزیک (کارشناسی ارشد),شیمی گرایش شیمی تجزیه (کارشناسی ارشد),فیزیک گرایش ذرات بنیادی و نظریه میدان ها (کارشناسی ارشد),فیزیک گرایش فیزیک هسته ای (کارشناسی ارشد),زیست شناسی گیاهی گرایش سلولی و تکوینی (کارشناسی ارشد),زیست شناسی گیاهی گرایش فیزیولوژی (کارشناسی ارشد)|ریاضی و آمار:ریاضی گرایش جبر (دکتری),ریاضی گرایش آنالیز (دکتری),آمار ریاضی (کارشناسی ارشد),ریاضیات و کاربرد ها (کارشناسی),علوم کامپیوتر (کارشناسی),ریاضی (دکتری),ریاضی کاربردی گرایش آنالیز عددی (کارشناسی ارشد)|فنی و مهندسی:مهندسی عمران گرایش سازه (دکتری),مهندسی برق گرایش قدرت (دکتری),مهندسی مکانیک گرایش تبدیل انرژی (دکتری)	1357		ادبیات و زبانهای خارجی, ادبیات و علوم انسانی, علوم پایه, ریاضی و آمار, فنی و مهندسی	کسب رتبه 25 در بین تمام دانشگاه های کشور در رتبه بندی جهانی یورپ 2022_2021\nکسب رتبه 18 در بین 25 دانشگاه جامع مورد تایید نظام رتبه بندی ISC در سال 2021\nکسب رتبه 8 در بین دانشگاه های جامع کشور در نظام رتبه بندی یواس نیوز سال 2022\nکسب رتبه بندی 501_600 دانشگاه های دنیا در نظام رتبه بندی تایمز سال 2022\nکسب رتبه 3 دانشگاه های جامع، رتبه 7 دانشگاه های کشور، رتبه 155 دانشگاه های جوان دنیا در سال 2021\n		0	0		35.6892	51.389	وزارت علوم	دانشگاه محقق اردبیلی	0	0		اردبیل	0	12000		دولتی		0	[]
\.


--
-- Data for Name: user_favorite_courses; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_favorite_courses (user_id, course_id) FROM stdin;
\.


--
-- Data for Name: user_follows; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_follows (id, created_at, status, follower_id, following_id) FROM stdin;
\.


--
-- Data for Name: user_profile_details; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_profile_details (id, academy_hashtags, academy_name, achievements, city, education, faculty, field_of_study, interests, is_teacher, province, skills, teaching_field, teaching_university, university, updated_at, work_experience, user_id, is_graduated) FROM stdin;
\.


--
-- Data for Name: user_profile_fields; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_profile_fields (profile_id, fields_of_study) FROM stdin;
\.


--
-- Data for Name: user_profile_universities; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_profile_universities (profile_id, universities) FROM stdin;
\.


--
-- Data for Name: user_sessions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_sessions (id, app_version, created_at, device_name, is_active, last_active_at, last_active_ip, os_version, platform, user_id) FROM stdin;
1117ae68-c2fb-4c7c-a252-28a1f54021b9	1.0.0	2026-05-12 04:14:14.641051+00	Xiaomi 2311DRK48G	t	2026-05-12 04:14:14.641072+00	\N	Android 15	Android	e7a11dde-6c43-480b-b0d7-daadd8648e90
c0e2c54e-f9ea-4671-b9df-5fb1c1d583f9	1.0.0	2026-05-12 11:25:28.958776+00	Xiaomi 2311DRK48G	t	2026-05-12 11:25:28.958783+00	\N	Android 15	Android	e7a11dde-6c43-480b-b0d7-daadd8648e90
dcc65bf4-3b28-40cb-9229-4f023250bc2e	1.0.0	2026-05-13 19:03:02.719598+00	Xiaomi 2311DRK48G	t	2026-05-13 19:03:02.719622+00	\N	Android 15	Android	e7a11dde-6c43-480b-b0d7-daadd8648e90
\.


--
-- Data for Name: user_subscriptions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_subscriptions (id, auto_renew, created_at, expires_at, is_active, starts_at, transaction_id, plan_id, user_id) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, avatar_url, bio, bio_channel_id1, bio_channel_id2, birth_date, created_at, display_name, educational_role, faculty, first_name, grade_level, institution_id, institution_logo_url, institution_name, is_online, is_premium, last_name, last_seen, major, national_code, online_visibility, password_hash, phone_number, phone_visibility, points, profile_visibility, role, username, average_rating, official_channel_id, review_count) FROM stdin;
e7a11dde-6c43-480b-b0d7-daadd8648e90	/uploads/c17899f1-2307-419d-97e0-cc35af9e4f11_edited_avatar_1694079316747707848.jpg	\N	\N	\N	\N	2026-05-12 04:14:14.602384+00	دیوید گاگینز	SCHOOL_STUDENT	\N	دیوید گاگینز	هنرستان	\N	\N	\N	t	f		2026-05-13 19:03:02.716251+00	\N	\N	EVERYONE	\N	09309663349	CONTACTS	0	EVERYONE	NORMAL	David	0	\N	0
\.


--
-- Data for Name: wallet_transactions; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.wallet_transactions (id, amount, balance_after, created_at, description, gateway_ref, reference_id, reference_type, type, wallet_id) FROM stdin;
\.


--
-- Data for Name: wallets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.wallets (id, balance, created_at, is_active, updated_at, user_id) FROM stdin;
\.


--
-- Name: ad_requests ad_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ad_requests
    ADD CONSTRAINT ad_requests_pkey PRIMARY KEY (id);


--
-- Name: ai_bot_messages ai_bot_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ai_bot_messages
    ADD CONSTRAINT ai_bot_messages_pkey PRIMARY KEY (id);


--
-- Name: ai_bots ai_bots_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ai_bots
    ADD CONSTRAINT ai_bots_pkey PRIMARY KEY (id);


--
-- Name: channel_post_comments channel_post_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_comments
    ADD CONSTRAINT channel_post_comments_pkey PRIMARY KEY (id);


--
-- Name: channel_post_reactions channel_post_reactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_reactions
    ADD CONSTRAINT channel_post_reactions_pkey PRIMARY KEY (id);


--
-- Name: channel_posts channel_posts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_posts
    ADD CONSTRAINT channel_posts_pkey PRIMARY KEY (id);


--
-- Name: channel_subscribers channel_subscribers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_subscribers
    ADD CONSTRAINT channel_subscribers_pkey PRIMARY KEY (id);


--
-- Name: channels channels_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channels
    ADD CONSTRAINT channels_pkey PRIMARY KEY (id);


--
-- Name: chats chats_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_pkey PRIMARY KEY (id);


--
-- Name: clubs clubs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.clubs
    ADD CONSTRAINT clubs_pkey PRIMARY KEY (id);


--
-- Name: collaboration_requests collaboration_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.collaboration_requests
    ADD CONSTRAINT collaboration_requests_pkey PRIMARY KEY (id);


--
-- Name: content_purchases content_purchases_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_purchases
    ADD CONSTRAINT content_purchases_pkey PRIMARY KEY (id);


--
-- Name: course_collaboration_requests course_collaboration_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_collaboration_requests
    ADD CONSTRAINT course_collaboration_requests_pkey PRIMARY KEY (id);


--
-- Name: course_comments course_comments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_comments
    ADD CONSTRAINT course_comments_pkey PRIMARY KEY (id);


--
-- Name: course_enrollments course_enrollments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_enrollments
    ADD CONSTRAINT course_enrollments_pkey PRIMARY KEY (id);


--
-- Name: course_materials course_materials_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_materials
    ADD CONSTRAINT course_materials_pkey PRIMARY KEY (id);


--
-- Name: courses courses_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT courses_pkey PRIMARY KEY (id);


--
-- Name: discounts discounts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.discounts
    ADD CONSTRAINT discounts_pkey PRIMARY KEY (id);


--
-- Name: education_levels education_levels_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.education_levels
    ADD CONSTRAINT education_levels_pkey PRIMARY KEY (id);


--
-- Name: educational_role_options educational_role_options_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.educational_role_options
    ADD CONSTRAINT educational_role_options_pkey PRIMARY KEY (id);


--
-- Name: elm_events elm_events_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.elm_events
    ADD CONSTRAINT elm_events_pkey PRIMARY KEY (id);


--
-- Name: entertainment_movies entertainment_movies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entertainment_movies
    ADD CONSTRAINT entertainment_movies_pkey PRIMARY KEY (id);


--
-- Name: entertainment_music entertainment_music_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entertainment_music
    ADD CONSTRAINT entertainment_music_pkey PRIMARY KEY (id);


--
-- Name: entertainment_riddles entertainment_riddles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entertainment_riddles
    ADD CONSTRAINT entertainment_riddles_pkey PRIMARY KEY (id);


--
-- Name: event_reports event_reports_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.event_reports
    ADD CONSTRAINT event_reports_pkey PRIMARY KEY (id);


--
-- Name: exam_access_rules exam_access_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_access_rules
    ADD CONSTRAINT exam_access_rules_pkey PRIMARY KEY (id);


--
-- Name: exam_answers exam_answers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_answers
    ADD CONSTRAINT exam_answers_pkey PRIMARY KEY (id);


--
-- Name: exam_attempts exam_attempts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_attempts
    ADD CONSTRAINT exam_attempts_pkey PRIMARY KEY (id);


--
-- Name: exam_question_options exam_question_options_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_question_options
    ADD CONSTRAINT exam_question_options_pkey PRIMARY KEY (id);


--
-- Name: exam_questions exam_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions
    ADD CONSTRAINT exam_questions_pkey PRIMARY KEY (id);


--
-- Name: exams exams_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT exams_pkey PRIMARY KEY (id);


--
-- Name: faculties faculties_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faculties
    ADD CONSTRAINT faculties_pkey PRIMARY KEY (id);


--
-- Name: feedbacks feedbacks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks
    ADD CONSTRAINT feedbacks_pkey PRIMARY KEY (id);


--
-- Name: fields_of_study fields_of_study_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.fields_of_study
    ADD CONSTRAINT fields_of_study_pkey PRIMARY KEY (id);


--
-- Name: group_members group_members_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT group_members_pkey PRIMARY KEY (id);


--
-- Name: group_message_reactions group_message_reactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_message_reactions
    ADD CONSTRAINT group_message_reactions_pkey PRIMARY KEY (id);


--
-- Name: group_messages group_messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_messages
    ADD CONSTRAINT group_messages_pkey PRIMARY KEY (id);


--
-- Name: groups groups_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);


--
-- Name: hashtag_promotions hashtag_promotions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hashtag_promotions
    ADD CONSTRAINT hashtag_promotions_pkey PRIMARY KEY (id);


--
-- Name: home_banners home_banners_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.home_banners
    ADD CONSTRAINT home_banners_pkey PRIMARY KEY (id);


--
-- Name: institution_honors institution_honors_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_honors
    ADD CONSTRAINT institution_honors_pkey PRIMARY KEY (id);


--
-- Name: institutions institutions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institutions
    ADD CONSTRAINT institutions_pkey PRIMARY KEY (id);


--
-- Name: locked_contents locked_contents_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.locked_contents
    ADD CONSTRAINT locked_contents_pkey PRIMARY KEY (id);


--
-- Name: message_reactions message_reactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_reactions
    ADD CONSTRAINT message_reactions_pkey PRIMARY KEY (id);


--
-- Name: messages messages_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT messages_pkey PRIMARY KEY (id);


--
-- Name: notifications notifications_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT notifications_pkey PRIMARY KEY (id);


--
-- Name: official_hashtags official_hashtags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.official_hashtags
    ADD CONSTRAINT official_hashtags_pkey PRIMARY KEY (id);


--
-- Name: otp_codes otp_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.otp_codes
    ADD CONSTRAINT otp_codes_pkey PRIMARY KEY (id);


--
-- Name: panel_admins panel_admins_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.panel_admins
    ADD CONSTRAINT panel_admins_pkey PRIMARY KEY (id);


--
-- Name: poll_options poll_options_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.poll_options
    ADD CONSTRAINT poll_options_pkey PRIMARY KEY (id);


--
-- Name: poll_votes poll_votes_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.poll_votes
    ADD CONSTRAINT poll_votes_pkey PRIMARY KEY (id);


--
-- Name: polls polls_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.polls
    ADD CONSTRAINT polls_pkey PRIMARY KEY (id);


--
-- Name: refresh_tokens refresh_tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_pkey PRIMARY KEY (id);


--
-- Name: riddle_options riddle_options_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.riddle_options
    ADD CONSTRAINT riddle_options_pkey PRIMARY KEY (id);


--
-- Name: role_channel_mappings role_channel_mappings_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_channel_mappings
    ADD CONSTRAINT role_channel_mappings_pkey PRIMARY KEY (id);


--
-- Name: smart_folder_rules smart_folder_rules_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.smart_folder_rules
    ADD CONSTRAINT smart_folder_rules_pkey PRIMARY KEY (id);


--
-- Name: startup_ideas startup_ideas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.startup_ideas
    ADD CONSTRAINT startup_ideas_pkey PRIMARY KEY (id);


--
-- Name: stories stories_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stories
    ADD CONSTRAINT stories_pkey PRIMARY KEY (id);


--
-- Name: story_replies story_replies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.story_replies
    ADD CONSTRAINT story_replies_pkey PRIMARY KEY (id);


--
-- Name: story_views story_views_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.story_views
    ADD CONSTRAINT story_views_pkey PRIMARY KEY (id);


--
-- Name: student_orgs student_orgs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.student_orgs
    ADD CONSTRAINT student_orgs_pkey PRIMARY KEY (id);


--
-- Name: subscription_plans subscription_plans_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.subscription_plans
    ADD CONSTRAINT subscription_plans_pkey PRIMARY KEY (id);


--
-- Name: teacher_verification_requests teacher_verification_requests_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.teacher_verification_requests
    ADD CONSTRAINT teacher_verification_requests_pkey PRIMARY KEY (id);


--
-- Name: users uk2f3xrn5enpplukjdl7e0c7rdf; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk2f3xrn5enpplukjdl7e0c7rdf UNIQUE (national_code);


--
-- Name: course_enrollments uk2ikyy9u68ld3vnvq089m4j1xg; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_enrollments
    ADD CONSTRAINT uk2ikyy9u68ld3vnvq089m4j1xg UNIQUE (course_id, user_id);


--
-- Name: official_hashtags uk3y3ngtm68muv5t6hrofjk75sd; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.official_hashtags
    ADD CONSTRAINT uk3y3ngtm68muv5t6hrofjk75sd UNIQUE (tag);


--
-- Name: faculties uk4u63apqkwoe8yh153mwyq93f5; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.faculties
    ADD CONSTRAINT uk4u63apqkwoe8yh153mwyq93f5 UNIQUE (name);


--
-- Name: users uk9q63snka3mdh91as4io72espi; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk9q63snka3mdh91as4io72espi UNIQUE (phone_number);


--
-- Name: user_follows ukac5tvpn8oynln4njypmqycil3; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_follows
    ADD CONSTRAINT ukac5tvpn8oynln4njypmqycil3 UNIQUE (follower_id, following_id);


--
-- Name: courses ukapi4hbdp2pqef23u5tvfkbl89; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT ukapi4hbdp2pqef23u5tvfkbl89 UNIQUE (channel_id);


--
-- Name: messages ukcnew2wif72cfckmy5x6xhvucg; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT ukcnew2wif72cfckmy5x6xhvucg UNIQUE (poll_id);


--
-- Name: courses ukcxlo9si45c8p0lipocn4k3hkp; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT ukcxlo9si45c8p0lipocn4k3hkp UNIQUE (group_id);


--
-- Name: group_messages ukd63cohwda4ej00skchel4o7cn; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_messages
    ADD CONSTRAINT ukd63cohwda4ej00skchel4o7cn UNIQUE (poll_id);


--
-- Name: exam_attempts ukdnkysdn4ur2w4nco3jy75ny91; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_attempts
    ADD CONSTRAINT ukdnkysdn4ur2w4nco3jy75ny91 UNIQUE (exam_id, user_id);


--
-- Name: channel_posts ukf3b9o5xibgmvxhl25hw2xlutg; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_posts
    ADD CONSTRAINT ukf3b9o5xibgmvxhl25hw2xlutg UNIQUE (poll_id);


--
-- Name: user_profile_details ukfkjs7h6bhtivttwm85gbworni; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profile_details
    ADD CONSTRAINT ukfkjs7h6bhtivttwm85gbworni UNIQUE (user_id);


--
-- Name: channels ukgu5ipm14t8fedcs6yl7byrh7g; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channels
    ADD CONSTRAINT ukgu5ipm14t8fedcs6yl7byrh7g UNIQUE (public_id);


--
-- Name: panel_admins ukh438gyvs401rc9p2pad3ptxyo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.panel_admins
    ADD CONSTRAINT ukh438gyvs401rc9p2pad3ptxyo UNIQUE (username);


--
-- Name: content_purchases ukm95s7fwxolty6deeuxukcn55d; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_purchases
    ADD CONSTRAINT ukm95s7fwxolty6deeuxukcn55d UNIQUE (content_id, user_id);


--
-- Name: institutions ukmd9krt5unslnbsj1hcjs7eo07; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institutions
    ADD CONSTRAINT ukmd9krt5unslnbsj1hcjs7eo07 UNIQUE (channel_id);


--
-- Name: users ukr43af9ap4edm43mmtq01oddj6; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT ukr43af9ap4edm43mmtq01oddj6 UNIQUE (username);


--
-- Name: wallets uksswfdl9fq40xlkove1y5kc7kv; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT uksswfdl9fq40xlkove1y5kc7kv UNIQUE (user_id);


--
-- Name: exam_answers uktpafefa1oahfiq95ily7cj9af; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_answers
    ADD CONSTRAINT uktpafefa1oahfiq95ily7cj9af UNIQUE (attempt_id, question_id);


--
-- Name: universities universities_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.universities
    ADD CONSTRAINT universities_pkey PRIMARY KEY (id);


--
-- Name: user_follows user_follows_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_follows
    ADD CONSTRAINT user_follows_pkey PRIMARY KEY (id);


--
-- Name: user_profile_details user_profile_details_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profile_details
    ADD CONSTRAINT user_profile_details_pkey PRIMARY KEY (id);


--
-- Name: user_sessions user_sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_sessions
    ADD CONSTRAINT user_sessions_pkey PRIMARY KEY (id);


--
-- Name: user_subscriptions user_subscriptions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT user_subscriptions_pkey PRIMARY KEY (id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: wallet_transactions wallet_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallet_transactions
    ADD CONSTRAINT wallet_transactions_pkey PRIMARY KEY (id);


--
-- Name: wallets wallets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (id);


--
-- Name: poll_options fk1baxdjoxricfu0grc0j6821f7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.poll_options
    ADD CONSTRAINT fk1baxdjoxricfu0grc0j6821f7 FOREIGN KEY (poll_id) REFERENCES public.polls(id);


--
-- Name: institution_manual_instructors fk1eqsn8epkjfxm4bo48s5t2ggm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_manual_instructors
    ADD CONSTRAINT fk1eqsn8epkjfxm4bo48s5t2ggm FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: refresh_tokens fk1lih5y2npsf8u5o3vhdb9y0os; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.refresh_tokens
    ADD CONSTRAINT fk1lih5y2npsf8u5o3vhdb9y0os FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: message_reactions fk1o714y33gam6b6741ci4ho041; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_reactions
    ADD CONSTRAINT fk1o714y33gam6b6741ci4ho041 FOREIGN KEY (message_id) REFERENCES public.messages(id);


--
-- Name: channel_post_comments fk1y9adiywwc0npxivkpqu37n40; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_comments
    ADD CONSTRAINT fk1y9adiywwc0npxivkpqu37n40 FOREIGN KEY (post_id) REFERENCES public.channel_posts(id);


--
-- Name: exam_access_rules fk2cylg6wpxq6moyf9t8ukp5sie; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_access_rules
    ADD CONSTRAINT fk2cylg6wpxq6moyf9t8ukp5sie FOREIGN KEY (exam_id) REFERENCES public.exams(id);


--
-- Name: course_manual_instructors fk2e62b8cmm0fjmw3x9x40k00f9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_manual_instructors
    ADD CONSTRAINT fk2e62b8cmm0fjmw3x9x40k00f9 FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: feedbacks fk312drfl5lquu37mu4trk8jkwx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks
    ADD CONSTRAINT fk312drfl5lquu37mu4trk8jkwx FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_subscriptions fk3l40lbyji8kj5xoc20ycwsc8g; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT fk3l40lbyji8kj5xoc20ycwsc8g FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: poll_votes fk3q0e7cabgif9f1t7voom07bg5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.poll_votes
    ADD CONSTRAINT fk3q0e7cabgif9f1t7voom07bg5 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: messages fk4ui4nnwntodh6wjvck53dbk9m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk4ui4nnwntodh6wjvck53dbk9m FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: exam_questions fk5cd6sjmccb11rrwpyabyc81c0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_questions
    ADD CONSTRAINT fk5cd6sjmccb11rrwpyabyc81c0 FOREIGN KEY (exam_id) REFERENCES public.exams(id);


--
-- Name: channel_post_comments fk5j6j7ps0xvonhqsx4tqvrc5x4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_comments
    ADD CONSTRAINT fk5j6j7ps0xvonhqsx4tqvrc5x4 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: exam_attempts fk5tomiinihc09ywy0wh15pi2cs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_attempts
    ADD CONSTRAINT fk5tomiinihc09ywy0wh15pi2cs FOREIGN KEY (exam_id) REFERENCES public.exams(id);


--
-- Name: exams fk637rqs62xkt9i8bs9v5924p5j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exams
    ADD CONSTRAINT fk637rqs62xkt9i8bs9v5924p5j FOREIGN KEY (creator_id) REFERENCES public.users(id);


--
-- Name: messages fk64w44ngcpqp99ptcb9werdfmb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fk64w44ngcpqp99ptcb9werdfmb FOREIGN KEY (chat_id) REFERENCES public.chats(id);


--
-- Name: course_chapters fk6e01epq13kxyl8na3qg9i48er; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_chapters
    ADD CONSTRAINT fk6e01epq13kxyl8na3qg9i48er FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: institutions fk6o9crbubg4ipb7qbc4923471b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institutions
    ADD CONSTRAINT fk6o9crbubg4ipb7qbc4923471b FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: teacher_verif_documents fk6sq1e3sviof2ebyp239pge81x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.teacher_verif_documents
    ADD CONSTRAINT fk6sq1e3sviof2ebyp239pge81x FOREIGN KEY (request_id) REFERENCES public.teacher_verification_requests(id);


--
-- Name: course_teachers fk7dtau95vh3vvh6oa5vt9kmgb9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_teachers
    ADD CONSTRAINT fk7dtau95vh3vvh6oa5vt9kmgb9 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: locked_contents fk7et442owsubvd5wv59kgkxmtm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.locked_contents
    ADD CONSTRAINT fk7et442owsubvd5wv59kgkxmtm FOREIGN KEY (uploader_id) REFERENCES public.users(id);


--
-- Name: content_purchases fk7evnrnxbmtpvbi302q4c983sh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_purchases
    ADD CONSTRAINT fk7evnrnxbmtpvbi302q4c983sh FOREIGN KEY (content_id) REFERENCES public.locked_contents(id);


--
-- Name: institution_clubs fk7qkidb8c1314g66awvb991tjs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_clubs
    ADD CONSTRAINT fk7qkidb8c1314g66awvb991tjs FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: course_teachers fk84v77efghiwpxgicxggm5rpdv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_teachers
    ADD CONSTRAINT fk84v77efghiwpxgicxggm5rpdv FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: startup_ideas fk8lkw1nw1dagbax1uxk1vxl94k; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.startup_ideas
    ADD CONSTRAINT fk8lkw1nw1dagbax1uxk1vxl94k FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: course_collaboration_requests fk8lumfamy4i3ofx0vjdmabaadp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_collaboration_requests
    ADD CONSTRAINT fk8lumfamy4i3ofx0vjdmabaadp FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: hashtag_promotions fk8q4dnfu3xme8pyloryg14ndki; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hashtag_promotions
    ADD CONSTRAINT fk8q4dnfu3xme8pyloryg14ndki FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: institution_universities fk8rcl4xml942b7gp8a3623o9tm; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_universities
    ADD CONSTRAINT fk8rcl4xml942b7gp8a3623o9tm FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: wallet_transactions fk8seu7b87ifqi09ghhssusmb0x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallet_transactions
    ADD CONSTRAINT fk8seu7b87ifqi09ghhssusmb0x FOREIGN KEY (wallet_id) REFERENCES public.wallets(id);


--
-- Name: collaboration_requests fk8sy06cfb9yh4vy2qpo0cewls0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.collaboration_requests
    ADD CONSTRAINT fk8sy06cfb9yh4vy2qpo0cewls0 FOREIGN KEY (receiver_id) REFERENCES public.users(id);


--
-- Name: poll_votes fk974fgfa4183h12b8vns9226qs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.poll_votes
    ADD CONSTRAINT fk974fgfa4183h12b8vns9226qs FOREIGN KEY (option_id) REFERENCES public.poll_options(id);


--
-- Name: notifications fk9y21adhxn0ayjhfocscqox7bh; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifications
    ADD CONSTRAINT fk9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: hashtag_promotions fka23k20n0admx37qma1uqod7t1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hashtag_promotions
    ADD CONSTRAINT fka23k20n0admx37qma1uqod7t1 FOREIGN KEY (hashtag_id) REFERENCES public.official_hashtags(id);


--
-- Name: group_message_reactions fka67xd9teyr9tka4wthuhmv40k; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_message_reactions
    ADD CONSTRAINT fka67xd9teyr9tka4wthuhmv40k FOREIGN KEY (message_id) REFERENCES public.group_messages(id);


--
-- Name: stories fkaofpeqba0lk3e3x10rtswuafb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stories
    ADD CONSTRAINT fkaofpeqba0lk3e3x10rtswuafb FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: exam_attempts fkb4cd93dglthtxs1o5yvm155e8; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_attempts
    ADD CONSTRAINT fkb4cd93dglthtxs1o5yvm155e8 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_profile_fields fkb64w2h3ly6hmd7fvjb2f227d6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profile_fields
    ADD CONSTRAINT fkb64w2h3ly6hmd7fvjb2f227d6 FOREIGN KEY (profile_id) REFERENCES public.user_profile_details(id);


--
-- Name: collaboration_requests fkb8s6vh4feyyhy0xojkm24kvx5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.collaboration_requests
    ADD CONSTRAINT fkb8s6vh4feyyhy0xojkm24kvx5 FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: chat_participants fkbhdyxo0ndtbs1t49l28y21rkw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT fkbhdyxo0ndtbs1t49l28y21rkw FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: institution_student_orgs fkbonj6ll0qayq6m7w0n4jax5fl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_student_orgs
    ADD CONSTRAINT fkbonj6ll0qayq6m7w0n4jax5fl FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: wallets fkc1foyisidw7wqqrkamafuwn4e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT fkc1foyisidw7wqqrkamafuwn4e FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_profile_universities fkc2jrs3gn26hum2jc1vtivteku; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profile_universities
    ADD CONSTRAINT fkc2jrs3gn26hum2jc1vtivteku FOREIGN KEY (profile_id) REFERENCES public.user_profile_details(id);


--
-- Name: channels fkc6sorav30ddgywp6vt99wen6x; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channels
    ADD CONSTRAINT fkc6sorav30ddgywp6vt99wen6x FOREIGN KEY (owner_id) REFERENCES public.users(id);


--
-- Name: exam_answers fkcgxivsvqp4ns0wtdsd5r1ymma; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_answers
    ADD CONSTRAINT fkcgxivsvqp4ns0wtdsd5r1ymma FOREIGN KEY (question_id) REFERENCES public.exam_questions(id);


--
-- Name: course_comments fkci2448eb50cohwai93f8a7sos; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_comments
    ADD CONSTRAINT fkci2448eb50cohwai93f8a7sos FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: group_messages fkcuf7bhtxj1qc0ajswsjrn9vvd; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_messages
    ADD CONSTRAINT fkcuf7bhtxj1qc0ajswsjrn9vvd FOREIGN KEY (sender_id) REFERENCES public.users(id);


--
-- Name: institutions fkd6og2gnj5d8md20wsnfdlbcse; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institutions
    ADD CONSTRAINT fkd6og2gnj5d8md20wsnfdlbcse FOREIGN KEY (owner_user_id) REFERENCES public.users(id);


--
-- Name: group_messages fkdcgpyr4nxl4ms6khvcenhxjvl; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_messages
    ADD CONSTRAINT fkdcgpyr4nxl4ms6khvcenhxjvl FOREIGN KEY (reply_to_id) REFERENCES public.group_messages(id);


--
-- Name: messages fkdniv96tu9qjcwk9y1a4ks8adi; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkdniv96tu9qjcwk9y1a4ks8adi FOREIGN KEY (poll_id) REFERENCES public.polls(id);


--
-- Name: ad_requests fkdx9w5hifpwrde4xjqvkbfeonn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ad_requests
    ADD CONSTRAINT fkdx9w5hifpwrde4xjqvkbfeonn FOREIGN KEY (requester_id) REFERENCES public.users(id);


--
-- Name: group_messages fkewowoij2kyue5i61sts3mbp35; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_messages
    ADD CONSTRAINT fkewowoij2kyue5i61sts3mbp35 FOREIGN KEY (group_id) REFERENCES public.groups(id);


--
-- Name: group_messages fkf1wquwh9atw9c59rkcqlbhth1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_messages
    ADD CONSTRAINT fkf1wquwh9atw9c59rkcqlbhth1 FOREIGN KEY (poll_id) REFERENCES public.polls(id);


--
-- Name: course_enrollments fkf78cq7ecdpk1clt1w5ofnb34t; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_enrollments
    ADD CONSTRAINT fkf78cq7ecdpk1clt1w5ofnb34t FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: polls fkfby9ehwb7k9qd4tupx30lea7l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.polls
    ADD CONSTRAINT fkfby9ehwb7k9qd4tupx30lea7l FOREIGN KEY (creator_id) REFERENCES public.users(id);


--
-- Name: course_admins fkfdbykdid6d83phsbwtr37p3qb; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_admins
    ADD CONSTRAINT fkfdbykdid6d83phsbwtr37p3qb FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: promotion_media_urls fkfijjg5origa882bcx108c726h; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.promotion_media_urls
    ADD CONSTRAINT fkfijjg5origa882bcx108c726h FOREIGN KEY (promotion_id) REFERENCES public.hashtag_promotions(id);


--
-- Name: course_admins fkfjbghx3r6ep688gnn9iphfoag; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_admins
    ADD CONSTRAINT fkfjbghx3r6ep688gnn9iphfoag FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: institution_instructors fkft3r6fs06uf2rv6ullxudke1a; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_instructors
    ADD CONSTRAINT fkft3r6fs06uf2rv6ullxudke1a FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: messages fkg23x99if9xk265onv7btb0cg9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.messages
    ADD CONSTRAINT fkg23x99if9xk265onv7btb0cg9 FOREIGN KEY (reply_to_id) REFERENCES public.messages(id);


--
-- Name: role_channel_mappings fkg7028o9294bd49ak2ycw0w11a; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.role_channel_mappings
    ADD CONSTRAINT fkg7028o9294bd49ak2ycw0w11a FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: story_replies fkg7n94vm1stfifnjrxd1pngo1l; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.story_replies
    ADD CONSTRAINT fkg7n94vm1stfifnjrxd1pngo1l FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: story_replies fkgf2fodch7tuaqdekgfstcl9uc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.story_replies
    ADD CONSTRAINT fkgf2fodch7tuaqdekgfstcl9uc FOREIGN KEY (story_id) REFERENCES public.stories(id);


--
-- Name: story_views fkggl5535ip5aofgxjp4q2chcw4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.story_views
    ADD CONSTRAINT fkggl5535ip5aofgxjp4q2chcw4 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_subscriptions fkgvwf73xtk31h777lq0wvk7u0w; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_subscriptions
    ADD CONSTRAINT fkgvwf73xtk31h777lq0wvk7u0w FOREIGN KEY (plan_id) REFERENCES public.subscription_plans(id);


--
-- Name: group_message_reactions fkhhi7x4y9xdpug7m3q890j6grn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_message_reactions
    ADD CONSTRAINT fkhhi7x4y9xdpug7m3q890j6grn FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: institution_honors fkhr4mqulcat6ypj6u36fvyhorg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_honors
    ADD CONSTRAINT fkhr4mqulcat6ypj6u36fvyhorg FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: teacher_verification_requests fkhsxn42pepqlty01tawto82v56; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.teacher_verification_requests
    ADD CONSTRAINT fkhsxn42pepqlty01tawto82v56 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: channel_post_reactions fki4u8dcplbnvba46qf28iw4sdq; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_reactions
    ADD CONSTRAINT fki4u8dcplbnvba46qf28iw4sdq FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: courses fkj8kcx9y3jrnr9swnq5ae8fw1e; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fkj8kcx9y3jrnr9swnq5ae8fw1e FOREIGN KEY (organizer_id) REFERENCES public.users(id);


--
-- Name: courses fkjnto55u59utnhr70mgvn7fpry; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fkjnto55u59utnhr70mgvn7fpry FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: course_materials fkjobqk7m872wjsw0y29tle6wek; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_materials
    ADD CONSTRAINT fkjobqk7m872wjsw0y29tle6wek FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: course_tags fkjqwlxw962j7q9wdogwnrctc2p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_tags
    ADD CONSTRAINT fkjqwlxw962j7q9wdogwnrctc2p FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: exam_question_options fkjsv1mb4vsgdpev0krclqjdfup; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_question_options
    ADD CONSTRAINT fkjsv1mb4vsgdpev0krclqjdfup FOREIGN KEY (question_id) REFERENCES public.exam_questions(id);


--
-- Name: groups fkkhpvhy2p2c1un4krvhwnau23b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.groups
    ADD CONSTRAINT fkkhpvhy2p2c1un4krvhwnau23b FOREIGN KEY (created_by) REFERENCES public.users(id);


--
-- Name: channel_posts fkkn5se5r0o8ui318xbtrnwpk1v; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_posts
    ADD CONSTRAINT fkkn5se5r0o8ui318xbtrnwpk1v FOREIGN KEY (poll_id) REFERENCES public.polls(id);


--
-- Name: institution_faculties fkkusv9wjf3qs5yuqq1oqttplc3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_faculties
    ADD CONSTRAINT fkkusv9wjf3qs5yuqq1oqttplc3 FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: group_members fkkv9vlrye4rmhqjq4qohy2n5a6; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT fkkv9vlrye4rmhqjq4qohy2n5a6 FOREIGN KEY (group_id) REFERENCES public.groups(id);


--
-- Name: content_purchases fkl6ff58hgh70kju0pbsd0614ry; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_purchases
    ADD CONSTRAINT fkl6ff58hgh70kju0pbsd0614ry FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: channel_subscribers fklfaw3x20ya2auahe7wdsgihhu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_subscribers
    ADD CONSTRAINT fklfaw3x20ya2auahe7wdsgihhu FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: channel_posts fklll1x3qpth3qh3odilhd61ufs; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_posts
    ADD CONSTRAINT fklll1x3qpth3qh3odilhd61ufs FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: panel_admin_permissions fklq6nwfl0oidtmva2cagysanyw; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.panel_admin_permissions
    ADD CONSTRAINT fklq6nwfl0oidtmva2cagysanyw FOREIGN KEY (admin_id) REFERENCES public.panel_admins(id);


--
-- Name: poll_votes fkmaogo469u92y072mev488em6p; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.poll_votes
    ADD CONSTRAINT fkmaogo469u92y072mev488em6p FOREIGN KEY (poll_id) REFERENCES public.polls(id);


--
-- Name: course_enrollments fkn0jagyiydh9aqty8r055q3kka; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_enrollments
    ADD CONSTRAINT fkn0jagyiydh9aqty8r055q3kka FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: chat_participants fkn4feij8janlba38q59kl2ebgg; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chat_participants
    ADD CONSTRAINT fkn4feij8janlba38q59kl2ebgg FOREIGN KEY (chat_id) REFERENCES public.chats(id);


--
-- Name: user_profile_details fkno5rk4mcuec78gns833fynkt9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_profile_details
    ADD CONSTRAINT fkno5rk4mcuec78gns833fynkt9 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: story_views fknpayqngtq4lo8dgqqpo01vcrv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.story_views
    ADD CONSTRAINT fknpayqngtq4lo8dgqqpo01vcrv FOREIGN KEY (story_id) REFERENCES public.stories(id);


--
-- Name: group_members fknr9qg33qt2ovmv29g4vc3gtdx; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_members
    ADD CONSTRAINT fknr9qg33qt2ovmv29g4vc3gtdx FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: locked_contents fko4u3kikwgd9sd8mbd6gb3p2qp; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.locked_contents
    ADD CONSTRAINT fko4u3kikwgd9sd8mbd6gb3p2qp FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: user_favorite_courses fko7xfed6t7ta19hq5eumk7csch; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_favorite_courses
    ADD CONSTRAINT fko7xfed6t7ta19hq5eumk7csch FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: institution_admins fkoc0tgltbko040kspat0blw8x9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_admins
    ADD CONSTRAINT fkoc0tgltbko040kspat0blw8x9 FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: course_suitable_for fkocuhex13578vvhvtef8tjxdxe; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_suitable_for
    ADD CONSTRAINT fkocuhex13578vvhvtef8tjxdxe FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: message_reactions fkoip2ttlg2py976foointttaew; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_reactions
    ADD CONSTRAINT fkoip2ttlg2py976foointttaew FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_follows fkp1rxuw1ulwo6mu84qaajuttrk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_follows
    ADD CONSTRAINT fkp1rxuw1ulwo6mu84qaajuttrk FOREIGN KEY (following_id) REFERENCES public.users(id);


--
-- Name: group_message_amplitudes fkp4fdsf8pbh5i22stq62vbjujo; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.group_message_amplitudes
    ADD CONSTRAINT fkp4fdsf8pbh5i22stq62vbjujo FOREIGN KEY (group_message_id) REFERENCES public.group_messages(id);


--
-- Name: exam_answers fkpcx0r6nsdbcx1pq8h7or1o8bc; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.exam_answers
    ADD CONSTRAINT fkpcx0r6nsdbcx1pq8h7or1o8bc FOREIGN KEY (attempt_id) REFERENCES public.exam_attempts(id);


--
-- Name: institution_fields fkpjdayk8nivx09f6be207sb3j9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_fields
    ADD CONSTRAINT fkpjdayk8nivx09f6be207sb3j9 FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: stories fkpqbkagok3xcwfhlbh1yo6o6vv; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stories
    ADD CONSTRAINT fkpqbkagok3xcwfhlbh1yo6o6vv FOREIGN KEY (group_id) REFERENCES public.groups(id);


--
-- Name: channel_subscribers fkq3oto6ecdra5vupoer9b7kek7; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_subscribers
    ADD CONSTRAINT fkq3oto6ecdra5vupoer9b7kek7 FOREIGN KEY (channel_id) REFERENCES public.channels(id);


--
-- Name: message_amplitudes fkqaqy8bakd9s6mfvks23ujrhg0; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.message_amplitudes
    ADD CONSTRAINT fkqaqy8bakd9s6mfvks23ujrhg0 FOREIGN KEY (message_id) REFERENCES public.messages(id);


--
-- Name: courses fkqeed8jx354sfrloky6s2iu6wa; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.courses
    ADD CONSTRAINT fkqeed8jx354sfrloky6s2iu6wa FOREIGN KEY (group_id) REFERENCES public.groups(id);


--
-- Name: event_reports fkqshamla9re3feu839yjwk1jgn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.event_reports
    ADD CONSTRAINT fkqshamla9re3feu839yjwk1jgn FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: user_follows fkqx9mu1fniaua5jfe1cdyspxdt; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_follows
    ADD CONSTRAINT fkqx9mu1fniaua5jfe1cdyspxdt FOREIGN KEY (follower_id) REFERENCES public.users(id);


--
-- Name: channel_post_reactions fkr1td7clel8efd316slxij2pmu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_reactions
    ADD CONSTRAINT fkr1td7clel8efd316slxij2pmu FOREIGN KEY (post_id) REFERENCES public.channel_posts(id);


--
-- Name: course_collaborators fkrerg2ouwopx5qjyvm9ifluar4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_collaborators
    ADD CONSTRAINT fkrerg2ouwopx5qjyvm9ifluar4 FOREIGN KEY (course_id) REFERENCES public.courses(id);


--
-- Name: channel_post_amplitudes fkrgnw9s3avf5fg78vigf0dipol; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.channel_post_amplitudes
    ADD CONSTRAINT fkrgnw9s3avf5fg78vigf0dipol FOREIGN KEY (channel_post_id) REFERENCES public.channel_posts(id);


--
-- Name: course_comments fksga1204uxye4tjjn2bn1marn9; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.course_comments
    ADD CONSTRAINT fksga1204uxye4tjjn2bn1marn9 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: stories fkshv2ytgbsn9w9mpu43mc6ln6j; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.stories
    ADD CONSTRAINT fkshv2ytgbsn9w9mpu43mc6ln6j FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: ad_requests fksr2itf2atkn9x3a6b9bbrd70w; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ad_requests
    ADD CONSTRAINT fksr2itf2atkn9x3a6b9bbrd70w FOREIGN KEY (target_channel_id) REFERENCES public.channels(id);


--
-- Name: riddle_options fksxod15lxv4yuvpib6klkstphk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.riddle_options
    ADD CONSTRAINT fksxod15lxv4yuvpib6klkstphk FOREIGN KEY (riddle_id) REFERENCES public.entertainment_riddles(id);


--
-- Name: institution_specialties fkt98e8iekxw5mni1uy7xaxk9w2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.institution_specialties
    ADD CONSTRAINT fkt98e8iekxw5mni1uy7xaxk9w2 FOREIGN KEY (institution_id) REFERENCES public.institutions(id);


--
-- Name: content_purchases fkta5ns2yisqf4o0jdnvsd4nt9w; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.content_purchases
    ADD CONSTRAINT fkta5ns2yisqf4o0jdnvsd4nt9w FOREIGN KEY (transaction_id) REFERENCES public.wallet_transactions(id);


--
-- PostgreSQL database dump complete
--

\unrestrict EMUAt7G9P9penhpF7PaOWuBv4rCJkLUSuQobHreK27gvMnJTIZPxxi40UlKqhnR

